package com.github.kr328.clash.home.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.glue.util.TABBY_RELEASES_LATEST
import com.github.kr328.clash.home.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

internal class HelpViewModel(app: Application) : AndroidViewModel(app) {
  val uiState: StateFlow<UiState>
    field = MutableStateFlow(UiState())

  val eventState: StateFlow<EventState>
    field = MutableStateFlow<EventState>(EventState.Idle)

  fun checkForUpdates() {
    if (uiState.value.checkingForUpdates) return

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
          return@launch
        }

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

  fun consumeEvent() {
    eventState.value = EventState.Idle
  }

  data class UiState(val checkingForUpdates: Boolean = false)

  sealed interface EventState {
    data object Idle : EventState

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
