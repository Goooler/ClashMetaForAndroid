package com.github.kr328.clash.home.vm

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.bridge.Bridge
import com.github.kr328.clash.core.util.trafficTotal
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.util.TABBY_RELEASES_LATEST
import com.github.kr328.clash.glue.util.startClashService
import com.github.kr328.clash.glue.util.stopClashService
import com.github.kr328.clash.glue.util.withClash
import com.github.kr328.clash.glue.util.withProfile
import com.github.kr328.clash.home.R
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

internal class HomeViewModel(app: Application) : AndroidViewModel(app), DefaultLifecycleObserver {
  private var broadcastEventsJob: Job? = null
  private var trafficPollingJob: Job? = null
  private var fetchJob: Job? = null

  val clashRunning: StateFlow<Boolean> = Remote.broadcasts.clashRunningFlow

  val uiState: StateFlow<UiState>
    field = MutableStateFlow(UiState())

  val eventState: StateFlow<EventState>
    field = MutableStateFlow<EventState>(EventState.Idle)

  override fun onStart(owner: LifecycleOwner) {
    broadcastEventsJob?.cancel()
    broadcastEventsJob = viewModelScope.launch {
      Remote.broadcasts.event.collect { event ->
        when (event) {
          ServiceRecreated,
          Started,
          ProfileChanged,
          ProfileLoaded -> fetch()
          is Stopped -> {
            event.cause?.let { message -> eventState.update { EventState.ShowMessage(message) } }
            fetch()
          }
          is ProfileUpdateCompleted,
          is ProfileUpdateFailed -> Unit
        }
      }
    }
    startTrafficPolling()
    fetch()
  }

  override fun onStop(owner: LifecycleOwner) {
    broadcastEventsJob?.cancel()
    broadcastEventsJob = null
    trafficPollingJob?.cancel()
    trafficPollingJob = null
  }

  fun toggleStatus() {
    if (clashRunning.value) {
      application.stopClashService()
    } else {
      startClash()
    }
  }

  fun showAbout() {
    viewModelScope.launch {
      val versionName =
        withContext(Dispatchers.IO) {
          application.packageManager.getPackageInfo(application.packageName, 0).versionName +
            "\n" +
            Bridge.nativeCoreVersion().replace("_", "-")
        }
      uiState.update { it.copy(aboutVersionName = versionName) }
    }
  }

  fun dismissAbout() {
    uiState.update { it.copy(aboutVersionName = null) }
  }

  fun checkForUpdates() {
    viewModelScope.launch {
      uiState.update { it.copy(checkingForUpdates = true) }
      try {
        val latestTag =
          withContext(Dispatchers.IO) {
            val request =
              Request.Builder()
                .url("https://api.github.com/repos/Goooler/Tabby/releases/latest")
                .header("Accept", "application/json")
                .build()
            OkHttpClient().newCall(request).execute().use { response ->
              if (!response.isSuccessful) return@withContext null
              releaseJson.decodeFromString<GithubRelease>(response.body.string()).tagName
            }
          }
        if (latestTag == null) {
          eventState.update {
            EventState.ShowMessage(application.getString(R.string.check_update_failed))
          }
        } else {
          val localVersion =
            application.packageManager.getPackageInfo(application.packageName, 0).versionName ?: ""
          val remoteVersion = latestTag.trimStart('v')
          if (isNewerVersion(remoteVersion, localVersion)) {
            eventState.update { EventState.UpdateAvailable(TABBY_RELEASES_LATEST) }
          } else {
            eventState.update {
              EventState.ShowMessage(application.getString(R.string.already_up_to_date))
            }
          }
        }
      } catch (e: Exception) {
        Log.e("Check for updates failed: ${e.message}", e)
        eventState.update {
          EventState.ShowMessage(application.getString(R.string.check_update_failed))
        }
      } finally {
        uiState.update { it.copy(checkingForUpdates = false) }
      }
    }
  }

  fun onVpnPermissionGranted() {
    application.startClashService()
  }

  fun consumeEvent() {
    eventState.value = EventState.Idle
  }

  private fun fetch() {
    fetchJob?.cancel()
    fetchJob = viewModelScope.launch {
      val state = withClash { queryTunnelState() }
      val providers = withClash { queryProviders() }
      val mode =
        when (state.mode) {
          Direct -> application.getString(CommonR.string.direct_mode)
          Global -> application.getString(CommonR.string.global_mode)
          Rule -> application.getString(CommonR.string.rule_mode)
        }
      val profileName = withProfile { queryActive()?.name }

      uiState.update {
        it.copy(
          mode = if (clashRunning.value) mode else null,
          hasProviders = providers.isNotEmpty(),
          profileName = profileName,
        )
      }
    }
  }

  private fun startTrafficPolling() {
    if (trafficPollingJob?.isActive == true) return
    trafficPollingJob = viewModelScope.launch {
      while (isActive) {
        delay(1.seconds)
        if (clashRunning.value) {
          val total = withClash { queryTrafficTotal() }
          uiState.update { it.copy(forwarded = total.trafficTotal()) }
        }
      }
    }
  }

  private fun startClash() {
    viewModelScope.launch {
      val active = withProfile { queryActive() }

      if (active == null || !active.imported) {
        eventState.value = EventState.ShowNoProfileMessage
        return@launch
      }

      try {
        val vpnRequest = application.startClashService()
        if (vpnRequest != null) {
          eventState.value = EventState.RequestVpnPermission(vpnRequest)
        }
      } catch (e: Exception) {
        Log.e("Start clash service failed: ${e.message}", e)
        eventState.value =
          EventState.ShowMessage(application.getString(CommonR.string.unable_to_start_vpn))
      }
    }
  }

  data class UiState(
    val forwarded: String? = null,
    val mode: String? = null,
    val profileName: String? = null,
    val hasProviders: Boolean = false,
    val aboutVersionName: String? = null,
    val checkingForUpdates: Boolean = false,
  )

  sealed interface EventState {
    data object Idle : EventState

    data class RequestVpnPermission(val intent: Intent) : EventState

    data object ShowNoProfileMessage : EventState

    data class ShowMessage(val message: String) : EventState

    data class UpdateAvailable(val releasesUrl: String) : EventState
  }

  @Serializable private data class GithubRelease(@SerialName("tag_name") val tagName: String)

  private companion object {
    val releaseJson = Json { ignoreUnknownKeys = true }

    fun isNewerVersion(remote: String, local: String): Boolean {
      val remoteParts = remote.split('.').mapNotNull { it.toIntOrNull() }
      val localParts = local.split('.').mapNotNull { it.toIntOrNull() }
      val maxLen = maxOf(remoteParts.size, localParts.size)
      for (i in 0 until maxLen) {
        val r = remoteParts.getOrElse(i) { 0 }
        val l = localParts.getOrElse(i) { 0 }
        if (r > l) return true
        if (r < l) return false
      }
      return false
    }
  }
}
