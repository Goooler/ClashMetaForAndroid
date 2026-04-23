package com.github.kr328.clash.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.github.kr328.clash.main.ui.HelpScreen
import com.github.kr328.clash.ui.Design
import com.github.kr328.clash.ui.DesignActivity
import com.github.kr328.clash.ui.theme.MihomoTheme

class HelpActivity : DesignActivity<Design.NoOp>() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent { MihomoTheme { HelpScreen(modifier = Modifier.fillMaxSize()) } }
  }
}
