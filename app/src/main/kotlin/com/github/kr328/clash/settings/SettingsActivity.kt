package com.github.kr328.clash.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.settings.ui.SettingsRoute
import com.github.kr328.clash.settings.ui.SettingsScreen
import com.github.kr328.clash.ui.BaseActivity
import com.github.kr328.clash.ui.theme.MihomoTheme

class SettingsActivity : BaseActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      MihomoTheme {
        SettingsScreen { route ->
          when (route) {
            SettingsRoute.App -> startActivity(AppSettingsActivity::class.intent)
            SettingsRoute.Network -> startActivity(NetworkSettingsActivity::class.intent)
            SettingsRoute.Override -> startActivity(OverrideSettingsActivity::class.intent)
            SettingsRoute.MetaFeature -> startActivity(MetaFeatureSettingsActivity::class.intent)
          }
        }
      }
    }
  }
}
