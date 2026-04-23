package com.github.kr328.clash.settings

import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.settings.ui.NetworkSettingsDesign
import com.github.kr328.clash.ui.BaseActivity
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select

class NetworkSettingsActivity : BaseActivity<NetworkSettingsDesign>() {
  override suspend fun main() {
    val design = NetworkSettingsDesign(this, uiStore, ServiceStore(this), clashRunning)

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
          when (it) {
            NetworkSettingsDesign.Request.StartAccessControlList ->
              startActivity(AccessControlActivity::class.intent)
          }
        }
      }
    }
  }
}
