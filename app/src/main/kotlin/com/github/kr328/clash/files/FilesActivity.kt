package com.github.kr328.clash.files

import android.os.Bundle
import androidx.activity.compose.setContent
import com.github.kr328.clash.common.util.uuid
import com.github.kr328.clash.files.ui.FilesScreen
import com.github.kr328.clash.ui.BaseActivity
import com.github.kr328.clash.ui.theme.MihomoTheme

class FilesActivity : BaseActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val uuid =
      intent.uuid
        ?: run {
          finish()
          return
        }

    setContent { MihomoTheme { FilesScreen(uuid = uuid, onFinish = ::finish) } }
  }
}
