package com.github.kr328.clash.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.github.kr328.clash.settings.ui.MetaFeatureSettingsScreen
import com.github.kr328.clash.settings.vm.MetaFeatureSettingsViewModel
import com.github.kr328.clash.ui.BaseActivity
import com.github.kr328.clash.ui.theme.MihomoTheme

class MetaFeatureSettingsActivity : BaseActivity() {
  private val viewModel by viewModels<MetaFeatureSettingsViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      MihomoTheme { MetaFeatureSettingsScreen(viewModel = viewModel, onResetCompleted = ::finish) }
    }
  }
}
