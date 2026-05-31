package com.github.kr328.clash.settings.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.VpnService
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.glue.model.DarkMode
import com.github.kr328.clash.settings.R
import com.github.kr328.clash.settings.vm.AppSettingsViewModel
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineBatterySaver
import com.github.kr328.clash.ui.icon.BaselineBrightness4
import com.github.kr328.clash.ui.icon.BaselineDomain
import com.github.kr328.clash.ui.icon.BaselineHide
import com.github.kr328.clash.ui.icon.BaselineRestore
import com.github.kr328.clash.ui.icon.BaselineStack
import com.github.kr328.clash.ui.icon.BaselineVpnLock
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.listPreference
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.switchPreference

@Composable
internal fun AppSettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: AppSettingsViewModel = viewModel(),
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val clashRunning by viewModel.clashRunning.collectAsStateWithLifecycle()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var vpnPermissionGranted by remember(context) { mutableStateOf(isVpnPermissionGranted(context)) }
  var batteryOptimizationIgnored by
    remember(context) { mutableStateOf(isBatteryOptimizationIgnored(context)) }

  val vpnPermissionLauncher =
    rememberLauncherForActivityResult(StartActivityForResult()) {
      vpnPermissionGranted = isVpnPermissionGranted(context)
    }
  val batteryOptimizationLauncher =
    rememberLauncherForActivityResult(StartActivityForResult()) {
      batteryOptimizationIgnored = isBatteryOptimizationIgnored(context)
    }

  DisposableEffect(context, lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        vpnPermissionGranted = isVpnPermissionGranted(context)
        batteryOptimizationIgnored = isBatteryOptimizationIgnored(context)
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)

    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  AppSettingsContent(
    clashRunning = clashRunning,
    uiState = uiState,
    vpnPermissionGranted = vpnPermissionGranted,
    batteryOptimizationIgnored = batteryOptimizationIgnored,
    onAutoRestartChange = viewModel::updateAutoRestart,
    onDarkModeChange = viewModel::updateDarkMode,
    onHideAppIconChange = viewModel::updateHideAppIcon,
    onHideFromRecentsChange = viewModel::updateHideFromRecents,
    onDynamicNotificationChange = viewModel::updateDynamicNotification,
    onAlwaysOnVpnChange = { enabled ->
      if (!enabled) return@AppSettingsContent

      val permissionIntent = VpnService.prepare(context)
      if (permissionIntent != null) {
        vpnPermissionLauncher.launch(permissionIntent)
      } else {
        context.startActivity(Intent(Settings.ACTION_VPN_SETTINGS))
      }
    },
    onIgnoreBatteryOptimizationChange = { enabled ->
      if (!enabled) return@AppSettingsContent

      batteryOptimizationLauncher.launch(
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
          .setData(Uri.parse("package:${context.packageName}"))
      )
    },
    modifier = modifier,
  )
}

@Composable
private fun AppSettingsContent(
  clashRunning: Boolean,
  uiState: AppSettingsViewModel.UiState,
  vpnPermissionGranted: Boolean,
  batteryOptimizationIgnored: Boolean,
  onAutoRestartChange: (Boolean) -> Unit,
  onDarkModeChange: (DarkMode) -> Unit,
  onHideAppIconChange: (Boolean) -> Unit,
  onHideFromRecentsChange: (Boolean) -> Unit,
  onDynamicNotificationChange: (Boolean) -> Unit,
  onAlwaysOnVpnChange: (Boolean) -> Unit,
  onIgnoreBatteryOptimizationChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  TabbyScaffold(title = stringResource(R.string.app), modifier = modifier) { innerPadding ->
    ProvidePreferenceLocals {
      LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        preferenceCategory(
          key = "cat_behavior",
          title = { Text(stringResource(R.string.behavior)) },
        )
        switchPreference(
          key = "auto_restart",
          value = uiState.autoRestart,
          onValueChange = onAutoRestartChange,
          icon = { Icon(imageVector = TabbyIcons.BaselineRestore, contentDescription = null) },
          title = { Text(stringResource(R.string.auto_restart)) },
          summary = { Text(stringResource(R.string.allow_tabby_auto_restart)) },
        )
        preferenceCategory(
          key = "cat_interface",
          title = { Text(stringResource(R.string.interface_)) },
        )
        listPreference(
          key = "dark_mode",
          value = uiState.darkMode,
          onValueChange = onDarkModeChange,
          values = listOf(DarkMode.Auto, DarkMode.ForceLight, DarkMode.ForceDark),
          icon = { Icon(imageVector = TabbyIcons.BaselineBrightness4, contentDescription = null) },
          title = { Text(stringResource(R.string.dark_mode)) },
          summary = { Text(stringResource(uiState.darkMode.summaryRes)) },
          valueToText = { androidx.compose.ui.text.AnnotatedString(stringResource(it.summaryRes)) },
        )
        switchPreference(
          key = "hide_app_icon",
          value = uiState.hideAppIcon,
          onValueChange = onHideAppIconChange,
          icon = { Icon(imageVector = TabbyIcons.BaselineHide, contentDescription = null) },
          title = { Text(stringResource(R.string.hide_app_icon_title)) },
          summary = { Text(stringResource(R.string.hide_app_icon_desc)) },
        )
        switchPreference(
          key = "hide_from_recents",
          value = uiState.hideFromRecents,
          onValueChange = onHideFromRecentsChange,
          icon = { Icon(imageVector = TabbyIcons.BaselineStack, contentDescription = null) },
          title = { Text(stringResource(R.string.hide_from_recents_title)) },
          summary = { Text(stringResource(R.string.hide_from_recents_desc)) },
        )
        preferenceCategory(key = "cat_service", title = { Text(stringResource(R.string.service)) })
        switchPreference(
          key = "show_traffic",
          value = uiState.dynamicNotification,
          onValueChange = onDynamicNotificationChange,
          enabled = !clashRunning,
          icon = { Icon(imageVector = TabbyIcons.BaselineDomain, contentDescription = null) },
          title = { Text(stringResource(R.string.show_traffic)) },
          summary = { Text(stringResource(R.string.show_traffic_summary)) },
        )
        switchPreference(
          key = "always_on_vpn",
          value = vpnPermissionGranted,
          onValueChange = onAlwaysOnVpnChange,
          enabled = !vpnPermissionGranted,
          icon = { Icon(imageVector = TabbyIcons.BaselineVpnLock, contentDescription = null) },
          title = { Text(stringResource(R.string.always_on_vpn)) },
          summary = { Text(stringResource(R.string.always_on_vpn_summary)) },
        )
        switchPreference(
          key = "ignore_battery_optimizations",
          value = batteryOptimizationIgnored,
          onValueChange = onIgnoreBatteryOptimizationChange,
          enabled = !batteryOptimizationIgnored,
          icon = { Icon(imageVector = TabbyIcons.BaselineBatterySaver, contentDescription = null) },
          title = { Text(stringResource(R.string.ignore_battery_optimizations)) },
          summary = { Text(stringResource(R.string.ignore_battery_optimizations_summary)) },
        )
      }
    }
  }
}

private val DarkMode.summaryRes: Int
  @StringRes
  get() =
    when (this) {
      Auto -> R.string.follow_system_android_10
      ForceLight -> R.string.always_light
      ForceDark -> R.string.always_dark
    }

@PreviewWrapper(TabbyThemeWrapper::class)
@PreviewTabby
@Composable
private fun AppSettingsScreenPreview() {
  AppSettingsContent(
    clashRunning = false,
    uiState =
      AppSettingsViewModel.UiState(
        autoRestart = true,
        darkMode = DarkMode.Auto,
        hideAppIcon = false,
        hideFromRecents = false,
        dynamicNotification = true,
      ),
    vpnPermissionGranted = false,
    batteryOptimizationIgnored = false,
    onAutoRestartChange = {},
    onDarkModeChange = {},
    onHideAppIconChange = {},
    onHideFromRecentsChange = {},
    onDynamicNotificationChange = {},
    onAlwaysOnVpnChange = {},
    onIgnoreBatteryOptimizationChange = {},
  )
}

@PreviewWrapper(TabbyThemeWrapper::class)
@PreviewTabby
@Composable
private fun AppSettingsScreenRunningPreview() {
  AppSettingsContent(
    clashRunning = true,
    uiState =
      AppSettingsViewModel.UiState(
        autoRestart = true,
        darkMode = DarkMode.ForceDark,
        hideAppIcon = true,
        hideFromRecents = true,
        dynamicNotification = true,
      ),
    vpnPermissionGranted = true,
    batteryOptimizationIgnored = true,
    onAutoRestartChange = {},
    onDarkModeChange = {},
    onHideAppIconChange = {},
    onHideFromRecentsChange = {},
    onDynamicNotificationChange = {},
    onAlwaysOnVpnChange = {},
    onIgnoreBatteryOptimizationChange = {},
  )
}

private fun isVpnPermissionGranted(context: Context): Boolean {
  return VpnService.prepare(context) == null
}

private fun isBatteryOptimizationIgnored(context: Context): Boolean {
  return context
    .getSystemService(PowerManager::class.java)
    ?.isIgnoringBatteryOptimizations(context.packageName) == true
}
