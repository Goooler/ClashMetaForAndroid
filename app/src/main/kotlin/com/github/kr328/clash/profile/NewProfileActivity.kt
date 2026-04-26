package com.github.kr328.clash.profile

import android.os.Bundle
import androidx.activity.compose.setContent
import com.github.kr328.clash.profile.ui.NewProfileScreen
import com.github.kr328.clash.ui.BaseActivity
import com.github.kr328.clash.ui.theme.MihomoTheme

class NewProfileActivity : BaseActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent { MihomoTheme { NewProfileScreen(onFinish = ::finish) } }
  }
}
