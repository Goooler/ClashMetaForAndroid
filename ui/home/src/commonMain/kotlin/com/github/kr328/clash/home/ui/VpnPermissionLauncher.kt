package com.github.kr328.clash.home.ui

import androidx.compose.runtime.Composable

internal class VpnPermissionRequest(internal val value: Any)

@Composable
internal expect fun rememberRequestVpnPermission(
  onResult: (granted: Boolean) -> Unit
): (VpnPermissionRequest) -> Unit
