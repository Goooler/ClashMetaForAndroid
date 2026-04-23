package com.github.kr328.clash.log

import android.os.Bundle
import androidx.activity.compose.setContent
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.common.util.setFileName
import com.github.kr328.clash.log.ui.LogsScreen
import com.github.kr328.clash.ui.BaseActivity
import com.github.kr328.clash.ui.theme.MihomoTheme

class LogsActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      MihomoTheme {
        LogsScreen(
          onStartLogcat = {
            startActivity(LogcatActivity::class.intent)
            finish()
          },
          onOpenFile = { startActivity(LogcatActivity::class.intent.setFileName(it.fileName)) },
        )
      }
    }
  }
}
