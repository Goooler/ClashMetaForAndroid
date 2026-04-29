package com.github.kr328.clash.settings.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.R
import com.github.kr328.clash.service.model.AccessControlMode
import com.github.kr328.clash.settings.vm.NetworkSettingsViewModel
import com.github.kr328.clash.ui.component.MihomoScaffold
import com.github.kr328.clash.ui.icon.BaselineVpnLock
import com.github.kr328.clash.ui.icon.MihomoIcons
import com.github.kr328.clash.ui.theme.MihomoThemeWrapper
import com.github.kr328.clash.ui.theme.PreviewMihomo
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.listPreference
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.switchPreference

@Composable
fun NetworkSettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: NetworkSettingsViewModel = viewModel(),
  onStartAccessControlList: () -> Unit,
) {
  val clashRunning by viewModel.clashRunning.collectAsStateWithLifecycle()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  NetworkSettingsContent(
    clashRunning = clashRunning,
    uiState = uiState,
    onEnableVpnChange = viewModel::updateEnableVpn,
    onBypassPrivateNetworkChange = viewModel::updateBypassPrivateNetwork,
    onDnsHijackingChange = viewModel::updateDnsHijacking,
    onAllowBypassChange = viewModel::updateAllowBypass,
    onAllowIpv6Change = viewModel::updateAllowIpv6,
    onSystemProxyChange = viewModel::updateSystemProxy,
    onTunStackModeChange = viewModel::updateTunStackMode,
    onAccessControlModeChange = viewModel::updateAccessControlMode,
    onAccessControlPackagesClick = onStartAccessControlList,
    modifier = modifier,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NetworkSettingsContent(
  clashRunning: Boolean,
  uiState: NetworkSettingsViewModel.UiState,
  onEnableVpnChange: (Boolean) -> Unit,
  onBypassPrivateNetworkChange: (Boolean) -> Unit,
  onDnsHijackingChange: (Boolean) -> Unit,
  onAllowBypassChange: (Boolean) -> Unit,
  onAllowIpv6Change: (Boolean) -> Unit,
  onSystemProxyChange: (Boolean) -> Unit,
  onTunStackModeChange: (String) -> Unit,
  onAccessControlModeChange: (AccessControlMode) -> Unit,
  onAccessControlPackagesClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val barMessage = stringResource(R.string.options_unavailable)

  LaunchedEffect(clashRunning) {
    if (clashRunning) {
      snackbarHostState.showSnackbar(
        message = barMessage,
        withDismissAction = true,
        duration = SnackbarDuration.Indefinite,
      )
    }
  }

  val vpnDependenciesEnabled = !clashRunning && uiState.enableVpn
  val tunStackMode = TunStackMode.fromValue(uiState.tunStackMode)

  MihomoScaffold(
    title = stringResource(R.string.network),
    modifier = modifier.fillMaxSize(),
    snackbarHostState = snackbarHostState,
  ) { innerPadding ->
    ProvidePreferenceLocals {
      LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        switchPreference(
          key = "route_system_traffic",
          value = uiState.enableVpn,
          onValueChange = onEnableVpnChange,
          enabled = !clashRunning,
          icon = { Icon(imageVector = MihomoIcons.BaselineVpnLock, contentDescription = null) },
          title = { Text(stringResource(R.string.route_system_traffic)) },
          summary = { Text(stringResource(R.string.routing_via_vpn_service)) },
        )

        preferenceCategory(
          key = "cat_vpn_service_options",
          title = { Text(stringResource(R.string.vpn_service_options)) },
        )

        switchPreference(
          key = "bypass_private_network",
          value = uiState.bypassPrivateNetwork,
          onValueChange = onBypassPrivateNetworkChange,
          enabled = vpnDependenciesEnabled,
          title = { Text(stringResource(R.string.bypass_private_network)) },
          summary = { Text(stringResource(R.string.bypass_private_network_summary)) },
        )
        switchPreference(
          key = "dns_hijacking",
          value = uiState.dnsHijacking,
          onValueChange = onDnsHijackingChange,
          enabled = vpnDependenciesEnabled,
          title = { Text(stringResource(R.string.dns_hijacking)) },
          summary = { Text(stringResource(R.string.dns_hijacking_summary)) },
        )
        switchPreference(
          key = "allow_bypass",
          value = uiState.allowBypass,
          onValueChange = onAllowBypassChange,
          enabled = vpnDependenciesEnabled,
          title = { Text(stringResource(R.string.allow_bypass)) },
          summary = { Text(stringResource(R.string.allow_bypass_summary)) },
        )
        switchPreference(
          key = "allow_ipv6",
          value = uiState.allowIpv6,
          onValueChange = onAllowIpv6Change,
          enabled = vpnDependenciesEnabled,
          title = { Text(stringResource(R.string.allow_ipv6)) },
          summary = { Text(stringResource(R.string.allow_ipv6_summary)) },
        )
        if (uiState.hasSystemProxyOption) {
          switchPreference(
            key = "system_proxy",
            value = uiState.systemProxy,
            onValueChange = onSystemProxyChange,
            enabled = vpnDependenciesEnabled,
            title = { Text(stringResource(R.string.system_proxy)) },
            summary = { Text(stringResource(R.string.system_proxy_summary)) },
          )
        }
        listPreference(
          key = "tun_stack_mode",
          value = tunStackMode,
          onValueChange = { onTunStackModeChange(it.persistedValue) },
          values = TunStackMode.entries,
          enabled = vpnDependenciesEnabled,
          title = { Text(stringResource(R.string.tun_stack_mode)) },
          summary = { Text(stringResource(tunStackMode.summaryRes)) },
          valueToText = { androidx.compose.ui.text.AnnotatedString(stringResource(it.summaryRes)) },
        )
        listPreference(
          key = "access_control_mode",
          value = uiState.accessControlMode,
          onValueChange = onAccessControlModeChange,
          values =
            listOf(
              AccessControlMode.AcceptAll,
              AccessControlMode.AcceptSelected,
              AccessControlMode.DenySelected,
            ),
          enabled = vpnDependenciesEnabled,
          title = { Text(stringResource(R.string.access_control_mode)) },
          summary = { Text(stringResource(uiState.accessControlMode.summaryRes)) },
          valueToText = { androidx.compose.ui.text.AnnotatedString(stringResource(it.summaryRes)) },
        )
        preference(
          key = "access_control_packages",
          modifier = Modifier.fillMaxWidth(),
          title = { Text(stringResource(R.string.access_control_packages)) },
          summary = { Text(stringResource(R.string.access_control_packages_summary)) },
          onClick = onAccessControlPackagesClick,
        )
      }
    }
  }
}

private enum class TunStackMode(val persistedValue: String) {
  System("system"),
  Gvisor("gvisor"),
  Mixed("mixed");

  companion object {
    fun fromValue(value: String): TunStackMode {
      return entries.firstOrNull { it.persistedValue == value } ?: System
    }
  }
}

private val TunStackMode.summaryRes: Int
  @StringRes
  get() =
    when (this) {
      TunStackMode.System -> R.string.tun_stack_system
      TunStackMode.Gvisor -> R.string.tun_stack_gvisor
      TunStackMode.Mixed -> R.string.tun_stack_mixed
    }

private val AccessControlMode.summaryRes: Int
  @StringRes
  get() =
    when (this) {
      AccessControlMode.AcceptAll -> R.string.allow_all_apps
      AccessControlMode.AcceptSelected -> R.string.allow_selected_apps
      AccessControlMode.DenySelected -> R.string.deny_selected_apps
    }

@PreviewWrapper(MihomoThemeWrapper::class)
@PreviewMihomo
@Composable
private fun NetworkSettingsScreenPreview() {
  NetworkSettingsContent(
    clashRunning = false,
    uiState =
      NetworkSettingsViewModel.UiState(
        hasSystemProxyOption = true,
        enableVpn = true,
        bypassPrivateNetwork = true,
        dnsHijacking = true,
        allowBypass = true,
        allowIpv6 = false,
        systemProxy = true,
        tunStackMode = "system",
        accessControlMode = AccessControlMode.AcceptAll,
      ),
    onEnableVpnChange = {},
    onBypassPrivateNetworkChange = {},
    onDnsHijackingChange = {},
    onAllowBypassChange = {},
    onAllowIpv6Change = {},
    onSystemProxyChange = {},
    onTunStackModeChange = {},
    onAccessControlModeChange = {},
    onAccessControlPackagesClick = {},
  )
}

@PreviewWrapper(MihomoThemeWrapper::class)
@PreviewMihomo
@Composable
private fun NetworkSettingsScreenRunningPreview() {
  NetworkSettingsContent(
    clashRunning = true,
    uiState =
      NetworkSettingsViewModel.UiState(
        hasSystemProxyOption = true,
        enableVpn = true,
        bypassPrivateNetwork = true,
        dnsHijacking = true,
        allowBypass = true,
        allowIpv6 = false,
        systemProxy = true,
        tunStackMode = "mixed",
        accessControlMode = AccessControlMode.DenySelected,
      ),
    onEnableVpnChange = {},
    onBypassPrivateNetworkChange = {},
    onDnsHijackingChange = {},
    onAllowBypassChange = {},
    onAllowIpv6Change = {},
    onSystemProxyChange = {},
    onTunStackModeChange = {},
    onAccessControlModeChange = {},
    onAccessControlPackagesClick = {},
  )
}
