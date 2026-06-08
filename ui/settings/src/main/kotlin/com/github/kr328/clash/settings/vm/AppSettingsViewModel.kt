package com.github.kr328.clash.settings.vm

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import com.github.kr328.clash.common.di.AppInfoProvider.Companion.instance as appInfoProvider
import com.github.kr328.clash.common.util.componentName
import com.github.kr328.clash.common.util.mainActivityAlias
import com.github.kr328.clash.glue.model.DarkMode
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.store.UiStore
import com.github.kr328.clash.glue.util.ApplicationObserver
import com.github.kr328.clash.service.store.ServiceStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

internal class AppSettingsViewModel(
  private val application: Application,
  private val uiStore: UiStore,
) : ViewModel() {
  private val serviceStore = ServiceStore(application)
  private val pm = application.packageManager
  private val restartReceiverClass = appInfoProvider.restartReceiverClass

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
      val status = pm.getComponentEnabledSetting(restartReceiverClass.componentName)
      return status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }
    set(value) {
      val status =
        if (value) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        else PackageManager.COMPONENT_ENABLED_STATE_DISABLED

      pm.setComponentEnabledSetting(
        restartReceiverClass.componentName,
        status,
        PackageManager.DONT_KILL_APP,
      )
    }

  private fun hideAppIcon(hide: Boolean) {
    val newState =
      if (hide) {
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED
      } else {
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED
      }
    pm.setComponentEnabledSetting(
      application.mainActivityAlias,
      newState,
      PackageManager.DONT_KILL_APP,
    )
  }

  data class UiState(
    val autoRestart: Boolean,
    val darkMode: DarkMode,
    val hideAppIcon: Boolean,
    val hideFromRecents: Boolean,
    val dynamicNotification: Boolean,
  )
}
