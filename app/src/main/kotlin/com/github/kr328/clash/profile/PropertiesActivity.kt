package com.github.kr328.clash.profile

import android.os.Bundle
import androidx.activity.compose.setContent
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.common.util.setUUID
import com.github.kr328.clash.common.util.uuid
import com.github.kr328.clash.files.FilesActivity
import com.github.kr328.clash.profile.ui.PropertiesScreen
import com.github.kr328.clash.ui.BaseActivity
import com.github.kr328.clash.ui.theme.MihomoTheme

class PropertiesActivity : BaseActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setResult(RESULT_CANCELED)

    val uuid = checkNotNull(intent.uuid) { "The uuid must not be null." }

    setContent {
      MihomoTheme {
        PropertiesScreen(
          uuid = uuid,
          onBrowseFiles = { profileUuid ->
            startActivity(FilesActivity::class.intent.setUUID(profileUuid))
          },
          onFinish = { success ->
            if (success) {
              setResult(RESULT_OK)
            }
            finish()
          },
        )
      }
    }
  }
}
