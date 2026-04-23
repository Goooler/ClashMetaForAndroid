package com.github.kr328.clash.settings

import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.settings.ui.OverrideSettingsDesign
import com.github.kr328.clash.ui.BaseActivity
import com.github.kr328.clash.util.withClash
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select

class OverrideSettingsActivity : BaseActivity<OverrideSettingsDesign>() {
  override suspend fun main() {
    val configuration = withClash { queryOverride(Clash.OverrideSlot.Persist) }

    defer { withClash { patchOverride(Clash.OverrideSlot.Persist, configuration) } }

    val design = OverrideSettingsDesign(this, configuration)

    setContentDesign(design)

    while (isActive) {
      select {
        events.onReceive {}

        design.requests.onReceive {
          when (it) {
            OverrideSettingsDesign.Request.ResetOverride -> {
              defer { withClash { clearOverride(Clash.OverrideSlot.Persist) } }
              finish()
            }
          }
        }
      }
    }
  }
}
