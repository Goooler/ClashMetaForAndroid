package com.github.kr328.clash.crash

import android.os.Bundle
import androidx.activity.compose.setContent
import com.github.kr328.clash.crash.ui.ApkBrokenScreen
import com.github.kr328.clash.ui.BaseActivity
import com.github.kr328.clash.ui.theme.MihomoTheme

class ApkBrokenActivity : BaseActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { MihomoTheme { ApkBrokenScreen() } }
  }
}
