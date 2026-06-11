package com.github.kr328.clash.settings.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.kr328.clash.service.model.AccessControlMode
import com.github.kr328.clash.settings.Res
import com.github.kr328.clash.settings.access_control_mode
import com.github.kr328.clash.settings.access_control_packages
import com.github.kr328.clash.settings.access_control_packages_summary
import com.github.kr328.clash.settings.allow_all_apps
import com.github.kr328.clash.settings.allow_bypass
import com.github.kr328.clash.settings.allow_bypass_summary
import com.github.kr328.clash.settings.allow_ipv6
import com.github.kr328.clash.settings.allow_ipv6_summary
import com.github.kr328.clash.settings.allow_selected_apps
import com.github.kr328.clash.settings.bypass_private_network
import com.github.kr328.clash.settings.bypass_private_network_summary
import com.github.kr328.clash.settings.deny_selected_apps
import com.github.kr328.clash.settings.dns_hijacking
import com.github.kr328.clash.settings.dns_hijacking_summary
import com.github.kr328.clash.settings.network
import com.github.kr328.clash.settings.options_unavailable
import com.github.kr328.clash.settings.route_system_traffic
import com.github.kr328.clash.settings.routing_via_vpn_service
import com.github.kr328.clash.settings.system_proxy
import com.github.kr328.clash.settings.system_proxy_summary
import com.github.kr328.clash.settings.tun_stack_gvisor
import com.github.kr328.clash.settings.tun_stack_mixed
import com.github.kr328.clash.settings.tun_stack_mode
import com.github.kr328.clash.settings.tun_stack_system
import com.github.kr328.clash.settings.vm.NetworkSettingsViewModel
import com.github.kr328.clash.settings.vpn_service_options
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineVpnLock
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper
import com.github.kr328.clash.ui.util.stringResCompat
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.listPreference
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.switchPreference
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun NetworkSettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: NetworkSettingsViewModel = koinViewModel(),
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
  val barMessage = stringResource(Res.string.options_unavailable)

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

  TabbyScaffold(
    title = stringResource(Res.string.network),
    modifier = modifier,
    snackbarHostState = snackbarHostState,
  ) { innerPadding ->
    ProvidePreferenceLocals {
      LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        switchPreference(
          key = "route_system_traffic",
          value = uiState.enableVpn,
          onValueChange = onEnableVpnChange,
          enabled = !clashRunning,
          icon = { Icon(imageVector = TabbyIcons.BaselineVpnLock, contentDescription = null) },
          title = { Text(stringResource(Res.string.route_system_traffic)) },
          summary = { Text(stringResource(Res.string.routing_via_vpn_service)) },
        )
        preferenceCategory(
          key = "cat_vpn_service_options",
          title = { Text(stringResource(Res.string.vpn_service_options)) },
        )
        switchPreference(
          key = "bypass_private_network",
          value = uiState.bypassPrivateNetwork,
          onValueChange = onBypassPrivateNetworkChange,
          enabled = vpnDependenciesEnabled,
          title = { Text(stringResource(Res.string.bypass_private_network)) },
          summary = { Text(stringResource(Res.string.bypass_private_network_summary)) },
        )
        switchPreference(
          key = "dns_hijacking",
          value = uiState.dnsHijacking,
          onValueChange = onDnsHijackingChange,
          enabled = vpnDependenciesEnabled,
          title = { Text(stringResource(Res.string.dns_hijacking)) },
          summary = { Text(stringResource(Res.string.dns_hijacking_summary)) },
        )
        switchPreference(
          key = "allow_bypass",
          value = uiState.allowBypass,
          onValueChange = onAllowBypassChange,
          enabled = vpnDependenciesEnabled,
          title = { Text(stringResource(Res.string.allow_bypass)) },
          summary = { Text(stringResource(Res.string.allow_bypass_summary)) },
        )
        switchPreference(
          key = "allow_ipv6",
          value = uiState.allowIpv6,
          onValueChange = onAllowIpv6Change,
          enabled = vpnDependenciesEnabled,
          title = { Text(stringResource(Res.string.allow_ipv6)) },
          summary = { Text(stringResource(Res.string.allow_ipv6_summary)) },
        )
        if (uiState.hasSystemProxyOption) {
          switchPreference(
            key = "system_proxy",
            value = uiState.systemProxy,
            onValueChange = onSystemProxyChange,
            enabled = vpnDependenciesEnabled,
            title = { Text(stringResource(Res.string.system_proxy)) },
            summary = { Text(stringResource(Res.string.system_proxy_summary)) },
          )
        }
        listPreference(
          key = "tun_stack_mode",
          value = tunStackMode,
          onValueChange = { onTunStackModeChange(it.persistedValue) },
          values = TunStackMode.entries,
          enabled = vpnDependenciesEnabled,
          title = { Text(stringResource(Res.string.tun_stack_mode)) },
          summary = { Text(stringResCompat(tunStackMode.summaryRes)) },
          valueToText = { AnnotatedString(stringResCompat(it.summaryRes)) },
        )
        listPreference(
          key = "access_control_mode",
          value = uiState.accessControlMode,
          onValueChange = onAccessControlModeChange,
          values = listOf(AcceptAll, AcceptSelected, DenySelected),
          enabled = vpnDependenciesEnabled,
          title = { Text(stringResource(Res.string.access_control_mode)) },
          summary = { Text(stringResCompat(uiState.accessControlMode.summaryRes)) },
          valueToText = { AnnotatedString(stringResCompat(it.summaryRes)) },
        )
        preference(
          key = "access_control_packages",
          title = { Text(stringResource(Res.string.access_control_packages)) },
          summary = { Text(stringResource(Res.string.access_control_packages_summary)) },
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

private val TunStackMode.summaryRes: Any
  get() =
    when (this) {
      TunStackMode.System -> Res.string.tun_stack_system
      Gvisor -> Res.string.tun_stack_gvisor
      Mixed -> Res.string.tun_stack_mixed
    }

private val AccessControlMode.summaryRes: Any
  get() =
    when (this) {
      AcceptAll -> Res.string.allow_all_apps
      AcceptSelected -> Res.string.allow_selected_apps
      DenySelected -> Res.string.deny_selected_apps
    }

@PreviewWrapper(TabbyThemeWrapper::class)
@PreviewTabby
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
        accessControlMode = AcceptAll,
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

@PreviewWrapper(TabbyThemeWrapper::class)
@PreviewTabby
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
        accessControlMode = DenySelected,
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
