package com.github.kr328.clash.settings.vm

import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.lifecycle.AndroidViewModel
import com.github.kr328.clash.RestartReceiver
import com.github.kr328.clash.common.util.componentName
import com.github.kr328.clash.model.DarkMode
import com.github.kr328.clash.remote.Remote
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.store.UiStore
import com.github.kr328.clash.store.UiStore.Companion.mainActivityAlias
import com.github.kr328.clash.util.ApplicationObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class AppSettingsViewModel(app: Application) : AndroidViewModel(app) {
  private val uiStore = UiStore(app)
  private val serviceStore = ServiceStore(app)
  private val pm = app.packageManager

  val clashRunning: StateFlow<Boolean> = Remote.broadcasts.clashRunningFlow

  val uiState: StateFlow<UiState>
    field =
      MutableStateFlow(
        UiState(
          autoRestart = autoRestartValue,
          darkMode = uiStore.darkMode,
          hideAppIcon = uiStore.hideAppIcon,
          hideFromRecents = uiStore.hideFromRecents,
          dynamicNotification = serviceStore.dynamicNotification,
        )
      )

  fun updateAutoRestart(value: Boolean) {
    autoRestartValue = value
    uiState.update { it.copy(autoRestart = value) }
  }

  fun updateDarkMode(value: DarkMode) {
    uiStore.darkMode = value
    uiState.update { it.copy(darkMode = value) }
  }

  fun updateHideAppIcon(value: Boolean) {
    hideAppIcon(value)
    uiStore.hideAppIcon = value
    uiState.update { it.copy(hideAppIcon = value) }
  }

  fun updateHideFromRecents(value: Boolean) {
    ApplicationObserver.createdActivities.forEach { it.recreate() }
    uiStore.hideFromRecents = value
    uiState.update { it.copy(hideFromRecents = value) }
  }

  fun updateDynamicNotification(value: Boolean) {
    serviceStore.dynamicNotification = value
    uiState.update { it.copy(dynamicNotification = value) }
  }

  private var autoRestartValue: Boolean
    get() {
      val status = pm.getComponentEnabledSetting(RestartReceiver::class.componentName)
      return status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }
    set(value) {
      val status =
        if (value) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        else PackageManager.COMPONENT_ENABLED_STATE_DISABLED

      pm.setComponentEnabledSetting(
        RestartReceiver::class.componentName,
        status,
        PackageManager.DONT_KILL_APP,
      )
    }

  private fun hideAppIcon(hide: Boolean) {
    val app = getApplication<Application>()
    val newState =
      if (hide) {
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED
      } else {
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED
      }
    pm.setComponentEnabledSetting(app.mainActivityAlias, newState, PackageManager.DONT_KILL_APP)
    if (hide) {
      // Prevent launcher activity not found.
      ShortcutManagerCompat.removeAllDynamicShortcuts(app)
    }
  }

  data class UiState(
    val autoRestart: Boolean,
    val darkMode: DarkMode,
    val hideAppIcon: Boolean,
    val hideFromRecents: Boolean,
    val dynamicNotification: Boolean,
  )
}
