package com.github.kr328.clash.home.vm

import android.app.Application
import android.content.Intent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.model.Traffic
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.core.util.trafficTotal
import com.github.kr328.clash.glue.remote.Broadcasts
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.util.startClashService
import com.github.kr328.clash.glue.util.stopClashService
import com.github.kr328.clash.glue.util.withClash
import com.github.kr328.clash.glue.util.withProfile
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

internal class HomeViewModel(private val dependencies: Dependencies) :
  ViewModel(), DefaultLifecycleObserver {
  private var broadcastEventsJob: Job? = null
  private var profileLoadedJob: Job? = null
  private var trafficPollingJob: Job? = null
  private var fetchJob: Job? = null

  val clashRunning: StateFlow<Boolean> = dependencies.clashRunning

  val uiState: StateFlow<UiState>
    field = MutableStateFlow(UiState())

  val eventState: StateFlow<EventState>
    field = MutableStateFlow<EventState>(EventState.Idle)

  override fun onStart(owner: LifecycleOwner) {
    broadcastEventsJob?.cancel()
    broadcastEventsJob = viewModelScope.launch {
      dependencies.events.collect { event ->
        when (event) {
          ServiceRecreated,
          Started,
          ProfileChanged -> fetch()
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
    profileLoadedJob?.cancel()
    profileLoadedJob = viewModelScope.launch {
      dependencies.profileLoaded.collect { loaded ->
        if (loaded) fetch() else clearRuntimeState()
      }
    }
    startTrafficPolling()
    fetch()
  }

  override fun onStop(owner: LifecycleOwner) {
    broadcastEventsJob?.cancel()
    broadcastEventsJob = null
    profileLoadedJob?.cancel()
    profileLoadedJob = null
    trafficPollingJob?.cancel()
    trafficPollingJob = null
  }

  fun toggleStatus() {
    if (clashRunning.value) {
      dependencies.stopClashService()
    } else {
      startClash()
    }
  }

  fun onVpnPermissionGranted() {
    dependencies.startClashService()
  }

  fun consumeEvent() {
    eventState.value = EventState.Idle
  }

  private fun fetch() {
    fetchJob?.cancel()
    fetchJob = viewModelScope.launch {
      if (!clashRunning.value || !dependencies.profileLoaded.value) {
        uiState.update {
          it.copy(
            mode = null,
            hasProviders = false,
          )
        }
      }

      val profileName =
        try {
          withTimeoutOrNull(200.milliseconds) { dependencies.queryActiveProfileName() }
        } catch (_: Exception) {
          null
        }

      if (!clashRunning.value || !dependencies.profileLoaded.value) {
        uiState.update {
          it.copy(
            mode = null,
            hasProviders = false,
            profileName = profileName ?: it.profileName,
          )
        }
        return@launch
      }

      val mode = dependencies.modeText(dependencies.queryMode())
      val hasProviders = dependencies.queryHasProviders()

      if (!clashRunning.value || !dependencies.profileLoaded.value) {
        uiState.update {
          it.copy(
            mode = null,
            hasProviders = false,
            profileName = profileName ?: it.profileName,
          )
        }
        return@launch
      }

      uiState.update {
        it.copy(
          mode = mode,
          hasProviders = hasProviders,
          profileName = profileName ?: it.profileName,
        )
      }
    }
  }

  private fun clearRuntimeState() {
    uiState.update {
      it.copy(
        mode = null,
        hasProviders = false,
      )
    }
  }

  private fun startTrafficPolling() {
    if (trafficPollingJob?.isActive == true) return
    trafficPollingJob = viewModelScope.launch {
      while (isActive) {
        delay(1.seconds)
        if (clashRunning.value) {
          val total = dependencies.queryTrafficTotal()
          uiState.update { it.copy(forwarded = total.trafficTotal()) }
        }
      }
    }
  }

  private fun startClash() {
    viewModelScope.launch {
      val hasProfile =
        try {
          withTimeoutOrNull(200.milliseconds) { dependencies.hasImportedActiveProfile() } ?: true
        } catch (_: Exception) {
          true
        }

      if (!hasProfile) {
        eventState.value = EventState.ShowNoProfileMessage
        return@launch
      }

      try {
        val vpnRequest = dependencies.startClashService()
        if (vpnRequest != null) {
          eventState.value = EventState.RequestVpnPermission(vpnRequest)
        }
      } catch (e: Exception) {
        Log.e("Start clash service failed: ${e.message}", e)
        eventState.value = EventState.ShowMessage(dependencies.unableToStartVpnText())
      }
    }
  }

  data class UiState(
    val forwarded: String? = null,
    val mode: String? = null,
    val profileName: String? = null,
    val hasProviders: Boolean = false,
  )

  sealed interface EventState {
    data object Idle : EventState

    data class RequestVpnPermission(val intent: Intent) : EventState

    data object ShowNoProfileMessage : EventState

    data class ShowMessage(val message: String) : EventState
  }

  interface Dependencies {
    val clashRunning: StateFlow<Boolean>

    val profileLoaded: StateFlow<Boolean>

    val events: Flow<Broadcasts.Event>

    suspend fun queryActiveProfileName(): String?

    suspend fun hasImportedActiveProfile(): Boolean

    suspend fun queryMode(): TunnelState.Mode

    suspend fun queryHasProviders(): Boolean

    suspend fun queryTrafficTotal(): Traffic

    fun modeText(mode: TunnelState.Mode): String

    fun unableToStartVpnText(): String

    fun startClashService(): Intent?

    fun stopClashService()
  }
}

internal class AndroidDependencies(private val application: Application) :
  HomeViewModel.Dependencies {
  override val clashRunning: StateFlow<Boolean> = Remote.broadcasts.clashRunningFlow
  override val profileLoaded: StateFlow<Boolean> = Remote.broadcasts.profileLoadedFlow
  override val events: Flow<Broadcasts.Event> = Remote.broadcasts.event

  override suspend fun queryActiveProfileName(): String? = withProfile { queryActive()?.name }

  override suspend fun hasImportedActiveProfile(): Boolean = withProfile {
    queryActive()?.imported == true
  }

  override suspend fun queryMode(): TunnelState.Mode = withClash { queryTunnelState().mode }

  override suspend fun queryHasProviders(): Boolean = withClash { queryProviders().isNotEmpty() }

  override suspend fun queryTrafficTotal(): Traffic = withClash { queryTrafficTotal() }

  override fun modeText(mode: TunnelState.Mode): String {
    return when (mode) {
      Direct -> application.getString(CommonR.string.direct_mode)
      Global -> application.getString(CommonR.string.global_mode)
      Rule -> application.getString(CommonR.string.rule_mode)
    }
  }

  override fun unableToStartVpnText(): String {
    return application.getString(CommonR.string.unable_to_start_vpn)
  }

  override fun startClashService(): Intent? = application.startClashService()

  override fun stopClashService() {
    application.stopClashService()
  }
}
