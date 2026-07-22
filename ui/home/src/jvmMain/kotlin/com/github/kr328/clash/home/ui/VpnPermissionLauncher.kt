package com.github.kr328.clash.home.ui

import androidx.compose.runtime.Composable

@Composable
internal actual fun rememberRequestVpnPermission(
  onResult: (granted: Boolean) -> Unit
): (VpnPermissionRequest) -> Unit = {}
