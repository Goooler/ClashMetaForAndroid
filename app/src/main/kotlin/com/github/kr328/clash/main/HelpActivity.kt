package com.github.kr328.clash.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.github.kr328.clash.main.ui.HelpScreen
import com.github.kr328.clash.ui.BaseActivity
import com.github.kr328.clash.ui.theme.MihomoTheme

class HelpActivity : BaseActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent { MihomoTheme { HelpScreen(modifier = Modifier.fillMaxSize()) } }
  }
}
