package com.github.kr328.clash.home.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.runtime.Composable

@Composable
internal actual fun rememberVpnPermissionLauncher(
  onResult: (granted: Boolean) -> Unit
): (VpnPermissionRequest) -> Unit {
  val launcher =
    rememberLauncherForActivityResult(StartActivityForResult()) { result ->
      onResult(result.resultCode == Activity.RESULT_OK)
    }

  return { request -> launcher.launch(request.value as Intent) }
}
