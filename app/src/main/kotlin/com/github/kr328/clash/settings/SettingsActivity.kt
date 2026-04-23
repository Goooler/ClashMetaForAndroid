package com.github.kr328.clash.settings

import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.settings.ui.SettingsDesign
import com.github.kr328.clash.ui.BaseActivity
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select

class SettingsActivity : BaseActivity<SettingsDesign>() {
  override suspend fun main() {
    val design = SettingsDesign(this)

    setContentDesign(design)

    while (isActive) {
      select {
        events.onReceive {}

        design.requests.onReceive {
          when (it) {
            SettingsDesign.Request.StartApp -> startActivity(AppSettingsActivity::class.intent)
            SettingsDesign.Request.StartNetwork ->
              startActivity(NetworkSettingsActivity::class.intent)

            SettingsDesign.Request.StartOverride ->
              startActivity(OverrideSettingsActivity::class.intent)

            SettingsDesign.Request.StartMetaFeature ->
              startActivity(MetaFeatureSettingsActivity::class.intent)
          }
        }
      }
    }
  }
}
