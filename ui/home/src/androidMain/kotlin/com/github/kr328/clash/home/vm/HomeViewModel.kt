package com.github.kr328.clash.home.vm

import android.app.Application
import android.content.Intent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.Res as CommonRes
import com.github.kr328.clash.common.direct_mode
import com.github.kr328.clash.common.global_mode
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.common.rule_mode
import com.github.kr328.clash.common.unable_to_start_vpn
import com.github.kr328.clash.core.model.Traffic
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.core.util.trafficTotal
import com.github.kr328.clash.glue.remote.Broadcasts
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.util.startClashService
import com.github.kr328.clash.glue.util.stopClashService
import com.github.kr328.clash.glue.util.withClash
import com.github.kr328.clash.glue.util.withProfile
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

internal class HomeViewModel(private val dependencies: Dependencies) :
  ViewModel(), DefaultLifecycleObserver {
  private var broadcastEventsJob: Job? = null
  private var profileLoadedJob: Job? = null
  private var trafficPollingJob: Job? = null
  private var fetchJob: Job? = null
  private var clashRunningJob: Job? = null
  private var transitionTimeoutJob: Job? = null
  private var lastClashRunning: Boolean? = null

  val clashRunning: StateFlow<Boolean> = dependencies.clashRunning

  val uiState: StateFlow<UiState>
    field = MutableStateFlow(UiState())

  val eventState: SharedFlow<EventState>
    field = MutableSharedFlow(extraBufferCapacity = 64)

  override fun onStart(owner: LifecycleOwner) {
    clashRunningJob?.cancel()
    clashRunningJob = viewModelScope.launch {
      dependencies.clashRunning.collect { running ->
        val last = lastClashRunning
        lastClashRunning = running
        if (last != null && last != running) {
          uiState.update { it.copy(isTransitioning = false) }
          cancelTransitionTimeout()
        }
      }
    }
    if (uiState.value.isTransitioning) {
      startTransitionTimeout()
    }
    broadcastEventsJob?.cancel()
    broadcastEventsJob = viewModelScope.launch {
      dependencies.events.collect { broadcastEvent ->
        when (broadcastEvent) {
          ServiceRecreated,
          Started,
          ProfileChanged -> fetch()
          ProfileLoaded -> fetch()
          is Stopped -> {
            broadcastEvent.cause?.let { message ->
              eventState.tryEmit(EventState.ShowMessage(message))
            }
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
    clashRunningJob?.cancel()
    clashRunningJob = null
    cancelTransitionTimeout()
    broadcastEventsJob?.cancel()
    broadcastEventsJob = null
    profileLoadedJob?.cancel()
    profileLoadedJob = null
    trafficPollingJob?.cancel()
    trafficPollingJob = null
  }

  fun toggleStatus() {
    if (uiState.value.isTransitioning) return

    uiState.update { it.copy(isTransitioning = true) }
    startTransitionTimeout()

    when (clashRunning.value) {
      true -> dependencies.stopClashService()
      false -> startClash()
    }
  }

  fun onVpnPermissionGranted() {
    dependencies.startClashService()
  }

  fun onVpnPermissionDenied() {
    uiState.update { it.copy(isTransitioning = false) }
    cancelTransitionTimeout()
  }

  private fun fetch() {
    fetchJob?.cancel()
    fetchJob = viewModelScope.launch {
      val profileName = dependencies.queryActiveProfileName()

      if (!clashRunning.value || !dependencies.profileLoaded.value) {
        uiState.update {
          it.copy(
            mode = null,
            hasProviders = false,
            profileName = profileName,
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
            profileName = profileName,
          )
        }
        return@launch
      }

      uiState.update {
        it.copy(
          mode = mode,
          hasProviders = hasProviders,
          profileName = profileName,
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
      if (!dependencies.hasImportedActiveProfile()) {
        eventState.tryEmit(EventState.ShowNoProfileMessage)
        uiState.update { it.copy(isTransitioning = false) }
        cancelTransitionTimeout()
        return@launch
      }

      try {
        val vpnRequest = dependencies.startClashService()
        if (vpnRequest != null) {
          eventState.tryEmit(EventState.RequestVpnPermission(vpnRequest))
        }
      } catch (e: Exception) {
        Log.e("Start clash service failed: ${e.message}", e)
        eventState.tryEmit(EventState.ShowMessage(dependencies.unableToStartVpnText()))
        uiState.update { it.copy(isTransitioning = false) }
        cancelTransitionTimeout()
      }
    }
  }

  private fun startTransitionTimeout() {
    transitionTimeoutJob?.cancel()
    transitionTimeoutJob = viewModelScope.launch {
      delay(10.seconds)
      uiState.update { it.copy(isTransitioning = false) }
    }
  }

  private fun cancelTransitionTimeout() {
    transitionTimeoutJob?.cancel()
    transitionTimeoutJob = null
  }

  data class UiState(
    val forwarded: String? = null,
    val mode: String? = null,
    val profileName: String? = null,
    val hasProviders: Boolean = false,
    val isTransitioning: Boolean = false,
  )

  sealed interface EventState {
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

    suspend fun modeText(mode: TunnelState.Mode): String

    suspend fun unableToStartVpnText(): String

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

  override suspend fun modeText(mode: TunnelState.Mode): String {
    return when (mode) {
      Direct -> getString(CommonRes.string.direct_mode)
      Global -> getString(CommonRes.string.global_mode)
      Rule -> getString(CommonRes.string.rule_mode)
    }
  }

  override suspend fun unableToStartVpnText(): String {
    return getString(CommonRes.string.unable_to_start_vpn)
  }

  override fun startClashService(): Intent? = application.startClashService()

  override fun stopClashService() {
    application.stopClashService()
  }
}
