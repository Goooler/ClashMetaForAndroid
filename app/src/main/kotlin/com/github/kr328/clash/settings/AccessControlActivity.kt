package com.github.kr328.clash.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.github.kr328.clash.settings.ui.AccessControlScreen
import com.github.kr328.clash.settings.vm.AccessControlViewModel
import com.github.kr328.clash.ui.BaseActivity
import com.github.kr328.clash.ui.theme.MihomoTheme

class AccessControlActivity : BaseActivity() {
  private val viewModel by viewModels<AccessControlViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent { MihomoTheme { AccessControlScreen(viewModel = viewModel) } }
  }
}
