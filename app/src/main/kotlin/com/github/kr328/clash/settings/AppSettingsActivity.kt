package com.github.kr328.clash.settings

import android.content.pm.PackageManager
import androidx.core.content.pm.ShortcutManagerCompat
import com.github.kr328.clash.RestartReceiver
import com.github.kr328.clash.common.util.componentName
import com.github.kr328.clash.model.Behavior
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.settings.ui.AppSettingsDesign
import com.github.kr328.clash.store.UiStore.Companion.mainActivityAlias
import com.github.kr328.clash.ui.DesignActivity
import com.github.kr328.clash.util.ApplicationObserver
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select

class AppSettingsActivity : DesignActivity<AppSettingsDesign>(), Behavior {
  override suspend fun main() {
    val design =
      AppSettingsDesign(this, uiStore, ServiceStore(this), this, clashRunning, ::onHideIconChange)

    setContentDesign(design)

    while (isActive) {
      select {
        events.onReceive {
          when (it) {
            Event.ClashStart,
            Event.ClashStop,
            Event.ServiceRecreated -> recreate()

            else -> Unit
          }
        }
        design.requests.onReceive {
          ApplicationObserver.createdActivities.forEach { it.recreate() }
        }
      }
    }
  }

  override var autoRestart: Boolean
    get() {
      val status = packageManager.getComponentEnabledSetting(RestartReceiver::class.componentName)

      return status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }
    set(value) {
      val status =
        if (value) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        else PackageManager.COMPONENT_ENABLED_STATE_DISABLED

      packageManager.setComponentEnabledSetting(
        RestartReceiver::class.componentName,
        status,
        PackageManager.DONT_KILL_APP,
      )
    }

  private fun onHideIconChange(hide: Boolean) {
    val newState =
      if (hide) {
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED
      } else {
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED
      }
    packageManager.setComponentEnabledSetting(
      mainActivityAlias,
      newState,
      PackageManager.DONT_KILL_APP,
    )
    if (hide) {
      // Prevent launcher activity not found.
      ShortcutManagerCompat.removeAllDynamicShortcuts(this)
    }
  }
}
