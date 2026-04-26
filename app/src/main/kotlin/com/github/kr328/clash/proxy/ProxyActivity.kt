package com.github.kr328.clash.proxy

import android.os.Bundle
import androidx.activity.compose.setContent
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.proxy.ui.ProxyScreen
import com.github.kr328.clash.ui.BaseActivity
import com.github.kr328.clash.ui.theme.MihomoTheme

class ProxyActivity : BaseActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      MihomoTheme {
        ProxyScreen(
          onReLaunch = {
            startActivity(ProxyActivity::class.intent)
            finish()
          }
        )
      }
    }
  }
}
