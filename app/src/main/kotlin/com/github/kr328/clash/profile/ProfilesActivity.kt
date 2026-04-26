package com.github.kr328.clash.profile

import android.os.Bundle
import androidx.activity.compose.setContent
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.common.util.setUUID
import com.github.kr328.clash.profile.ui.ProfilesScreen
import com.github.kr328.clash.ui.BaseActivity
import com.github.kr328.clash.ui.theme.MihomoTheme

class ProfilesActivity : BaseActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      MihomoTheme {
        ProfilesScreen(
          onOpenCreate = { startActivity(NewProfileActivity::class.intent) },
          onOpenEdit = { uuid -> startActivity(PropertiesActivity::class.intent.setUUID(uuid)) },
        )
      }
    }
  }
}
