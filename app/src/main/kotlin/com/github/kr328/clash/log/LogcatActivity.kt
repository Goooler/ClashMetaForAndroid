package com.github.kr328.clash.log

import android.os.Bundle
import androidx.activity.compose.setContent
import com.github.kr328.clash.common.util.fileName
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.log.ui.LogcatScreen
import com.github.kr328.clash.ui.BaseActivity
import com.github.kr328.clash.ui.theme.MihomoTheme

class LogcatActivity : BaseActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      MihomoTheme {
        LogcatScreen(
          fileName = intent?.fileName,
          onOpenLogs = {
            startActivity(LogsActivity::class.intent)
            finish()
          },
          onInvalidFile = ::finish,
          onClose = ::finish,
        )
      }
    }
  }
}
