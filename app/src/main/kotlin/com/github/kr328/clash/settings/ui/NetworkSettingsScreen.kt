package com.github.kr328.clash.settings.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.R
import com.github.kr328.clash.service.model.AccessControlMode
import com.github.kr328.clash.settings.vm.NetworkSettingsViewModel
import com.github.kr328.clash.ui.component.MihomoScaffold
import com.github.kr328.clash.ui.component.SettingsCategoryTitle
import com.github.kr328.clash.ui.component.SettingsPreferenceClickableItem
import com.github.kr328.clash.ui.component.SettingsPreferenceSwitchItem
import com.github.kr328.clash.ui.icon.BaselineVpnLock
import com.github.kr328.clash.ui.icon.MihomoIcons
import com.github.kr328.clash.ui.theme.MihomoTheme
import com.github.kr328.clash.ui.theme.PreviewMihomo

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
  var showTunStackDialog by remember { mutableStateOf(false) }
  var showAccessControlModeDialog by remember { mutableStateOf(false) }
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
    Column(
      modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())
    ) {
      SettingsPreferenceSwitchItem(
        icon = MihomoIcons.BaselineVpnLock,
        titleRes = R.string.route_system_traffic,
        summaryRes = R.string.routing_via_vpn_service,
        checked = uiState.enableVpn,
        enabled = !clashRunning,
        onCheckedChange = onEnableVpnChange,
      )

      SettingsCategoryTitle(text = stringResource(R.string.vpn_service_options))

      SettingsPreferenceSwitchItem(
        titleRes = R.string.bypass_private_network,
        summaryRes = R.string.bypass_private_network_summary,
        checked = uiState.bypassPrivateNetwork,
        enabled = vpnDependenciesEnabled,
        onCheckedChange = onBypassPrivateNetworkChange,
      )
      SettingsPreferenceSwitchItem(
        titleRes = R.string.dns_hijacking,
        summaryRes = R.string.dns_hijacking_summary,
        checked = uiState.dnsHijacking,
        enabled = vpnDependenciesEnabled,
        onCheckedChange = onDnsHijackingChange,
      )
      SettingsPreferenceSwitchItem(
        titleRes = R.string.allow_bypass,
        summaryRes = R.string.allow_bypass_summary,
        checked = uiState.allowBypass,
        enabled = vpnDependenciesEnabled,
        onCheckedChange = onAllowBypassChange,
      )
      SettingsPreferenceSwitchItem(
        titleRes = R.string.allow_ipv6,
        summaryRes = R.string.allow_ipv6_summary,
        checked = uiState.allowIpv6,
        enabled = vpnDependenciesEnabled,
        onCheckedChange = onAllowIpv6Change,
      )
      if (uiState.hasSystemProxyOption) {
        SettingsPreferenceSwitchItem(
          titleRes = R.string.system_proxy,
          summaryRes = R.string.system_proxy_summary,
          checked = uiState.systemProxy,
          enabled = vpnDependenciesEnabled,
          onCheckedChange = onSystemProxyChange,
        )
      }
      SettingsPreferenceClickableItem(
        titleRes = R.string.tun_stack_mode,
        summaryRes = tunStackMode.summaryRes,
        enabled = vpnDependenciesEnabled,
        onClick = { showTunStackDialog = true },
      )
      SettingsPreferenceClickableItem(
        titleRes = R.string.access_control_mode,
        summaryRes = uiState.accessControlMode.summaryRes,
        enabled = vpnDependenciesEnabled,
        onClick = { showAccessControlModeDialog = true },
      )
      SettingsPreferenceClickableItem(
        titleRes = R.string.access_control_packages,
        summaryRes = R.string.access_control_packages_summary,
        onClick = onAccessControlPackagesClick,
      )
    }
  }

  if (showTunStackDialog) {
    AlertDialog(
      onDismissRequest = { showTunStackDialog = false },
      title = { Text(text = stringResource(R.string.tun_stack_mode)) },
      text = {
        Column {
          TunStackMode.entries.forEach { value ->
            Row(
              modifier =
                Modifier.fillMaxWidth().clickable {
                  showTunStackDialog = false
                  onTunStackModeChange(value.persistedValue)
                },
              verticalAlignment = Alignment.CenterVertically,
            ) {
              RadioButton(selected = tunStackMode == value, onClick = null)
              Text(
                text = stringResource(value.summaryRes),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp),
              )
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showTunStackDialog = false }) {
          Text(text = stringResource(R.string.ok))
        }
      },
    )
  }

  if (showAccessControlModeDialog) {
    val accessControlModeItems =
      listOf(
        AccessControlMode.AcceptAll to R.string.allow_all_apps,
        AccessControlMode.AcceptSelected to R.string.allow_selected_apps,
        AccessControlMode.DenySelected to R.string.deny_selected_apps,
      )

    AlertDialog(
      onDismissRequest = { showAccessControlModeDialog = false },
      title = { Text(text = stringResource(R.string.access_control_mode)) },
      text = {
        Column {
          accessControlModeItems.forEach { (value, textRes) ->
            Row(
              modifier =
                Modifier.fillMaxWidth().clickable {
                  showAccessControlModeDialog = false
                  onAccessControlModeChange(value)
                },
              verticalAlignment = Alignment.CenterVertically,
            ) {
              RadioButton(selected = uiState.accessControlMode == value, onClick = null)
              Text(
                text = stringResource(textRes),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp),
              )
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showAccessControlModeDialog = false }) {
          Text(text = stringResource(R.string.ok))
        }
      },
    )
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

@PreviewMihomo
@Composable
private fun NetworkSettingsScreenPreview() {
  MihomoTheme {
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
}

@PreviewMihomo
@Composable
private fun NetworkSettingsScreenRunningPreview() {
  MihomoTheme {
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
}
