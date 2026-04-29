package com.github.kr328.clash.settings.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.R
import com.github.kr328.clash.model.DarkMode
import com.github.kr328.clash.settings.vm.AppSettingsViewModel
import com.github.kr328.clash.ui.component.MihomoScaffold
import com.github.kr328.clash.ui.icon.BaselineBrightness4
import com.github.kr328.clash.ui.icon.BaselineDomain
import com.github.kr328.clash.ui.icon.BaselineHide
import com.github.kr328.clash.ui.icon.BaselineRestore
import com.github.kr328.clash.ui.icon.BaselineStack
import com.github.kr328.clash.ui.icon.MihomoIcons
import com.github.kr328.clash.ui.theme.MihomoTheme
import com.github.kr328.clash.ui.theme.PreviewMihomo
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.listPreference
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.switchPreference

@Composable
fun AppSettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: AppSettingsViewModel = viewModel(),
) {
  val clashRunning by viewModel.clashRunning.collectAsStateWithLifecycle()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  AppSettingsContent(
    clashRunning = clashRunning,
    uiState = uiState,
    onAutoRestartChange = viewModel::updateAutoRestart,
    onDarkModeChange = viewModel::updateDarkMode,
    onHideAppIconChange = viewModel::updateHideAppIcon,
    onHideFromRecentsChange = viewModel::updateHideFromRecents,
    onDynamicNotificationChange = viewModel::updateDynamicNotification,
    modifier = modifier,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppSettingsContent(
  clashRunning: Boolean,
  uiState: AppSettingsViewModel.UiState,
  onAutoRestartChange: (Boolean) -> Unit,
  onDarkModeChange: (DarkMode) -> Unit,
  onHideAppIconChange: (Boolean) -> Unit,
  onHideFromRecentsChange: (Boolean) -> Unit,
  onDynamicNotificationChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  MihomoScaffold(title = stringResource(R.string.app), modifier = modifier.fillMaxSize()) {
    innerPadding ->
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
          icon = { Icon(imageVector = MihomoIcons.BaselineRestore, contentDescription = null) },
          title = { Text(stringResource(R.string.auto_restart)) },
          summary = { Text(stringResource(R.string.allow_clash_auto_restart)) },
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
          icon = { Icon(imageVector = MihomoIcons.BaselineBrightness4, contentDescription = null) },
          title = { Text(stringResource(R.string.dark_mode)) },
          summary = { Text(stringResource(uiState.darkMode.summaryRes)) },
          valueToText = { androidx.compose.ui.text.AnnotatedString(stringResource(it.summaryRes)) },
        )
        switchPreference(
          key = "hide_app_icon",
          value = uiState.hideAppIcon,
          onValueChange = onHideAppIconChange,
          icon = { Icon(imageVector = MihomoIcons.BaselineHide, contentDescription = null) },
          title = { Text(stringResource(R.string.hide_app_icon_title)) },
          summary = { Text(stringResource(R.string.hide_app_icon_desc)) },
        )
        switchPreference(
          key = "hide_from_recents",
          value = uiState.hideFromRecents,
          onValueChange = onHideFromRecentsChange,
          icon = { Icon(imageVector = MihomoIcons.BaselineStack, contentDescription = null) },
          title = { Text(stringResource(R.string.hide_from_recents_title)) },
          summary = { Text(stringResource(R.string.hide_from_recents_desc)) },
        )

        preferenceCategory(key = "cat_service", title = { Text(stringResource(R.string.service)) })
        switchPreference(
          key = "show_traffic",
          value = uiState.dynamicNotification,
          onValueChange = onDynamicNotificationChange,
          enabled = !clashRunning,
          icon = { Icon(imageVector = MihomoIcons.BaselineDomain, contentDescription = null) },
          title = { Text(stringResource(R.string.show_traffic)) },
          summary = { Text(stringResource(R.string.show_traffic_summary)) },
        )
      }
    }
  }
}

private val DarkMode.summaryRes: Int
  @StringRes
  get() =
    when (this) {
      DarkMode.Auto -> R.string.follow_system_android_10
      DarkMode.ForceLight -> R.string.always_light
      DarkMode.ForceDark -> R.string.always_dark
    }

@PreviewMihomo
@Composable
private fun AppSettingsScreenPreview() {
  MihomoTheme {
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
      onAutoRestartChange = {},
      onDarkModeChange = {},
      onHideAppIconChange = {},
      onHideFromRecentsChange = {},
      onDynamicNotificationChange = {},
    )
  }
}

@PreviewMihomo
@Composable
private fun AppSettingsScreenRunningPreview() {
  MihomoTheme {
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
      onAutoRestartChange = {},
      onDarkModeChange = {},
      onHideAppIconChange = {},
      onHideFromRecentsChange = {},
      onDynamicNotificationChange = {},
    )
  }
}
