package com.github.kr328.clash.home.vm

import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.github.kr328.clash.core.model.Traffic
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.glue.remote.Broadcasts
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {
  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  @Test
  fun uiState_whenServiceStartsBeforeProfileLoaded_thenHideMode() = runTest {
    val dependencies =
      TestHomeDependencies().apply {
        clashRunning.value = true
        profileLoaded.value = false
        mode = TunnelState.Mode.Rule
      }
    val viewModel = HomeViewModel(dependencies)

    try {
      viewModel.onStart(UnusedLifecycleOwner)
      dependencies.eventsFlow.emit(Broadcasts.Event.Started)

      assertEquals(null, viewModel.uiState.value.mode)
      assertEquals(0, dependencies.queryModeCalls)

      dependencies.mode = TunnelState.Mode.Global
      dependencies.profileLoaded.value = true

      assertEquals("Global Mode", viewModel.uiState.value.mode)
      assertEquals(1, dependencies.queryModeCalls)
    } finally {
      viewModel.onStop(UnusedLifecycleOwner)
    }
  }

  @Test
  fun uiState_whenProfileLoadedStateIsAlreadyTrue_thenShowMode() = runTest {
    val dependencies =
      TestHomeDependencies().apply {
        clashRunning.value = true
        profileLoaded.value = true
        mode = TunnelState.Mode.Global
      }
    val viewModel = HomeViewModel(dependencies)

    try {
      viewModel.onStart(UnusedLifecycleOwner)

      assertEquals("Global Mode", viewModel.uiState.value.mode)
    } finally {
      viewModel.onStop(UnusedLifecycleOwner)
    }
  }

  @Test
  fun uiState_whenProfileLoadedEventIsReceived_thenRefreshMode() = runTest {
    val dependencies =
      TestHomeDependencies().apply {
        clashRunning.value = true
        profileLoaded.value = true
        mode = TunnelState.Mode.Rule
      }
    val viewModel = HomeViewModel(dependencies)

    try {
      viewModel.onStart(UnusedLifecycleOwner)
      assertEquals("Rule Mode", viewModel.uiState.value.mode)

      dependencies.mode = TunnelState.Mode.Global
      dependencies.eventsFlow.emit(Broadcasts.Event.ProfileLoaded)

      assertEquals("Global Mode", viewModel.uiState.value.mode)
    } finally {
      viewModel.onStop(UnusedLifecycleOwner)
    }
  }

  private object UnusedLifecycleOwner : LifecycleOwner {
    override val lifecycle = LifecycleRegistry(this)
  }

  private class TestHomeDependencies : HomeViewModel.Dependencies {
    override val clashRunning = MutableStateFlow(false)
    override val profileLoaded = MutableStateFlow(false)
    val eventsFlow = MutableSharedFlow<Broadcasts.Event>(extraBufferCapacity = 16)
    override val events: Flow<Broadcasts.Event> = eventsFlow

    var mode: TunnelState.Mode = TunnelState.Mode.Rule
    var queryModeCalls = 0

    override suspend fun queryActiveProfileName(): String = "Profile"

    override suspend fun hasImportedActiveProfile(): Boolean = true

    override suspend fun queryMode(): TunnelState.Mode {
      queryModeCalls += 1
      return mode
    }

    override suspend fun queryHasProviders(): Boolean = true

    override suspend fun queryTrafficTotal(): Traffic = Traffic(0)

    override fun modeText(mode: TunnelState.Mode): String {
      return when (mode) {
        TunnelState.Mode.Direct -> "Direct Mode"
        TunnelState.Mode.Global -> "Global Mode"
        TunnelState.Mode.Rule -> "Rule Mode"
      }
    }

    override fun unableToStartVpnText(): String = "Unable to start VPN"

    override fun startClashService(): Intent? = null

    override fun stopClashService() = Unit
  }
}
