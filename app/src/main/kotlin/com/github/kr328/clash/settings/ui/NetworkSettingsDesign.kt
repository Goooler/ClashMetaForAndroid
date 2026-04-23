package com.github.kr328.clash.settings.ui

import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.R
import com.github.kr328.clash.service.model.AccessControlMode
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.store.UiStore
import com.github.kr328.clash.ui.Design
import com.github.kr328.clash.ui.SnackbarDuration
import com.github.kr328.clash.ui.component.SettingsCategoryTitle
import com.github.kr328.clash.ui.component.SettingsCommonScreen
import com.github.kr328.clash.ui.component.SettingsPreferenceClickableItem
import com.github.kr328.clash.ui.component.SettingsPreferenceSwitchItem
import com.github.kr328.clash.ui.theme.MihomoTheme
import com.github.kr328.clash.ui.theme.PreviewMihomo

class NetworkSettingsDesign(
  context: Context,
  private val uiStore: UiStore,
  private val serviceStore: ServiceStore,
  private val running: Boolean,
) : Design<NetworkSettingsDesign.Request>(context) {
  sealed interface Request {
    data object StartAccessControlList : Request
  }

  @Composable
  override fun Content() = MihomoTheme {
    NetworkSettingsScreen(
      running = running,
      hasSystemProxyOption = Build.VERSION.SDK_INT >= 29,
      enableVpnInitial = uiStore.enableVpn,
      bypassPrivateNetworkInitial = serviceStore.bypassPrivateNetwork,
      dnsHijackingInitial = serviceStore.dnsHijacking,
      allowBypassInitial = serviceStore.allowBypass,
      allowIpv6Initial = serviceStore.allowIpv6,
      systemProxyInitial = serviceStore.systemProxy,
      tunStackModeInitial = serviceStore.tunStackMode,
      accessControlModeInitial = serviceStore.accessControlMode,
      onEnableVpnChange = { uiStore.enableVpn = it },
      onBypassPrivateNetworkChange = { serviceStore.bypassPrivateNetwork = it },
      onDnsHijackingChange = { serviceStore.dnsHijacking = it },
      onAllowBypassChange = { serviceStore.allowBypass = it },
      onAllowIpv6Change = { serviceStore.allowIpv6 = it },
      onSystemProxyChange = { serviceStore.systemProxy = it },
      onTunStackModeChange = { serviceStore.tunStackMode = it },
      onAccessControlModeChange = { serviceStore.accessControlMode = it },
      onAccessControlPackagesClick = { requests.trySend(Request.StartAccessControlList) },
    )
  }

  init {
    if (running) {
      snackbar(R.string.options_unavailable, SnackbarDuration.Indefinite)
    }
  }
}

@Composable
private fun NetworkSettingsScreen(
  running: Boolean,
  hasSystemProxyOption: Boolean,
  enableVpnInitial: Boolean,
  bypassPrivateNetworkInitial: Boolean,
  dnsHijackingInitial: Boolean,
  allowBypassInitial: Boolean,
  allowIpv6Initial: Boolean,
  systemProxyInitial: Boolean,
  tunStackModeInitial: String,
  accessControlModeInitial: AccessControlMode,
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
  var enableVpn by remember { mutableStateOf(enableVpnInitial) }
  var bypassPrivateNetwork by remember { mutableStateOf(bypassPrivateNetworkInitial) }
  var dnsHijacking by remember { mutableStateOf(dnsHijackingInitial) }
  var allowBypass by remember { mutableStateOf(allowBypassInitial) }
  var allowIpv6 by remember { mutableStateOf(allowIpv6Initial) }
  var systemProxy by remember { mutableStateOf(systemProxyInitial) }
  var tunStackMode by remember { mutableStateOf(TunStackMode.fromValue(tunStackModeInitial)) }
  var accessControlMode by remember { mutableStateOf(accessControlModeInitial) }

  var showTunStackDialog by remember { mutableStateOf(false) }
  var showAccessControlModeDialog by remember { mutableStateOf(false) }

  val vpnDependenciesEnabled = !running && enableVpn

  SettingsCommonScreen(
    title = stringResource(R.string.network),
    modifier = modifier.fillMaxSize(),
  ) {
    SettingsPreferenceSwitchItem(
      iconRes = R.drawable.ic_baseline_vpn_lock,
      titleRes = R.string.route_system_traffic,
      summaryRes = R.string.routing_via_vpn_service,
      checked = enableVpn,
      enabled = !running,
      onCheckedChange = {
        enableVpn = it
        onEnableVpnChange(it)
      },
    )

    SettingsCategoryTitle(text = stringResource(R.string.vpn_service_options))

    SettingsPreferenceSwitchItem(
      titleRes = R.string.bypass_private_network,
      summaryRes = R.string.bypass_private_network_summary,
      checked = bypassPrivateNetwork,
      enabled = vpnDependenciesEnabled,
      onCheckedChange = {
        bypassPrivateNetwork = it
        onBypassPrivateNetworkChange(it)
      },
    )
    SettingsPreferenceSwitchItem(
      titleRes = R.string.dns_hijacking,
      summaryRes = R.string.dns_hijacking_summary,
      checked = dnsHijacking,
      enabled = vpnDependenciesEnabled,
      onCheckedChange = {
        dnsHijacking = it
        onDnsHijackingChange(it)
      },
    )
    SettingsPreferenceSwitchItem(
      titleRes = R.string.allow_bypass,
      summaryRes = R.string.allow_bypass_summary,
      checked = allowBypass,
      enabled = vpnDependenciesEnabled,
      onCheckedChange = {
        allowBypass = it
        onAllowBypassChange(it)
      },
    )
    SettingsPreferenceSwitchItem(
      titleRes = R.string.allow_ipv6,
      summaryRes = R.string.allow_ipv6_summary,
      checked = allowIpv6,
      enabled = vpnDependenciesEnabled,
      onCheckedChange = {
        allowIpv6 = it
        onAllowIpv6Change(it)
      },
    )
    if (hasSystemProxyOption) {
      SettingsPreferenceSwitchItem(
        titleRes = R.string.system_proxy,
        summaryRes = R.string.system_proxy_summary,
        checked = systemProxy,
        enabled = vpnDependenciesEnabled,
        onCheckedChange = {
          systemProxy = it
          onSystemProxyChange(it)
        },
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
      summaryRes = accessControlMode.summaryRes,
      enabled = vpnDependenciesEnabled,
      onClick = { showAccessControlModeDialog = true },
    )
    SettingsPreferenceClickableItem(
      titleRes = R.string.access_control_packages,
      summaryRes = R.string.access_control_packages_summary,
      onClick = onAccessControlPackagesClick,
    )
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
                  tunStackMode = value
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
                  accessControlMode = value
                  showAccessControlModeDialog = false
                  onAccessControlModeChange(value)
                },
              verticalAlignment = Alignment.CenterVertically,
            ) {
              RadioButton(selected = accessControlMode == value, onClick = null)
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
    NetworkSettingsScreen(
      running = false,
      hasSystemProxyOption = true,
      enableVpnInitial = true,
      bypassPrivateNetworkInitial = true,
      dnsHijackingInitial = true,
      allowBypassInitial = true,
      allowIpv6Initial = false,
      systemProxyInitial = true,
      tunStackModeInitial = "system",
      accessControlModeInitial = AccessControlMode.AcceptAll,
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
    NetworkSettingsScreen(
      running = true,
      hasSystemProxyOption = true,
      enableVpnInitial = true,
      bypassPrivateNetworkInitial = true,
      dnsHijackingInitial = true,
      allowBypassInitial = true,
      allowIpv6Initial = false,
      systemProxyInitial = true,
      tunStackModeInitial = "mixed",
      accessControlModeInitial = AccessControlMode.DenySelected,
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
