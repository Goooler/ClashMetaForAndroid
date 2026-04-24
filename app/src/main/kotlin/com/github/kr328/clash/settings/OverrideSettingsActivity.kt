package com.github.kr328.clash.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import com.github.kr328.clash.settings.ui.OverrideSettingsScreen
import com.github.kr328.clash.ui.BaseActivity
import com.github.kr328.clash.ui.theme.MihomoTheme

class OverrideSettingsActivity : BaseActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent { MihomoTheme { OverrideSettingsScreen(onResetCompleted = ::finish) } }
  }
}
