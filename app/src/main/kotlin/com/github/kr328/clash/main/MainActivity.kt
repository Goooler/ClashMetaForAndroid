package com.github.kr328.clash.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.log.LogcatActivity
import com.github.kr328.clash.log.LogcatService
import com.github.kr328.clash.log.LogsActivity
import com.github.kr328.clash.main.ui.MainScreen
import com.github.kr328.clash.profile.ProfilesActivity
import com.github.kr328.clash.profile.ProvidersActivity
import com.github.kr328.clash.proxy.ProxyActivity
import com.github.kr328.clash.settings.SettingsActivity
import com.github.kr328.clash.ui.BaseActivity
import com.github.kr328.clash.ui.theme.MihomoTheme

class MainActivity : BaseActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      MihomoTheme {
        MainScreen(
          onOpenProxy = { startActivity(ProxyActivity::class.intent) },
          onOpenProfiles = { startActivity(ProfilesActivity::class.intent) },
          onOpenProviders = { startActivity(ProvidersActivity::class.intent) },
          onOpenLogs = {
            if (LogcatService.running) {
              startActivity(LogcatActivity::class.intent)
            } else {
              startActivity(LogsActivity::class.intent)
            }
          },
          onOpenSettings = { startActivity(SettingsActivity::class.intent) },
          onOpenHelp = { startActivity(HelpActivity::class.intent) },
        )
      }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
      if (
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
          PackageManager.PERMISSION_GRANTED
      ) {
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }
}
