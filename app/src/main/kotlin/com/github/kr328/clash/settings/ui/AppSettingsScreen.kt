package com.github.kr328.clash.settings.ui

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.R
import com.github.kr328.clash.model.DarkMode
import com.github.kr328.clash.settings.vm.AppSettingsViewModel
import com.github.kr328.clash.ui.component.SettingsCategoryTitle
import com.github.kr328.clash.ui.component.SettingsCommonScreen
import com.github.kr328.clash.ui.component.SettingsPreferenceClickableItem
import com.github.kr328.clash.ui.component.SettingsPreferenceSwitchItem
import com.github.kr328.clash.ui.theme.MihomoTheme
import com.github.kr328.clash.ui.theme.PreviewMihomo

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
  var showDarkModeDialog by remember { mutableStateOf(false) }

  SettingsCommonScreen(title = stringResource(R.string.app), modifier = modifier.fillMaxSize()) {
    SettingsCategoryTitle(text = stringResource(R.string.behavior))
    SettingsPreferenceSwitchItem(
      iconRes = R.drawable.ic_baseline_restore,
      titleRes = R.string.auto_restart,
      summaryRes = R.string.allow_clash_auto_restart,
      checked = uiState.autoRestart,
      onCheckedChange = onAutoRestartChange,
    )

    SettingsCategoryTitle(text = stringResource(R.string.interface_))
    SettingsPreferenceClickableItem(
      iconRes = R.drawable.ic_baseline_brightness_4,
      titleRes = R.string.dark_mode,
      summaryRes = uiState.darkMode.summaryRes,
      onClick = { showDarkModeDialog = true },
    )
    SettingsPreferenceSwitchItem(
      iconRes = R.drawable.ic_baseline_hide,
      titleRes = R.string.hide_app_icon_title,
      summaryRes = R.string.hide_app_icon_desc,
      checked = uiState.hideAppIcon,
      onCheckedChange = onHideAppIconChange,
    )
    SettingsPreferenceSwitchItem(
      iconRes = R.drawable.ic_baseline_stack,
      titleRes = R.string.hide_from_recents_title,
      summaryRes = R.string.hide_from_recents_desc,
      checked = uiState.hideFromRecents,
      onCheckedChange = onHideFromRecentsChange,
    )

    SettingsCategoryTitle(text = stringResource(R.string.service))
    SettingsPreferenceSwitchItem(
      iconRes = R.drawable.ic_baseline_domain,
      titleRes = R.string.show_traffic,
      summaryRes = R.string.show_traffic_summary,
      checked = uiState.dynamicNotification,
      enabled = !clashRunning,
      onCheckedChange = onDynamicNotificationChange,
    )
  }

  if (showDarkModeDialog) {
    val darkModeItems =
      listOf(
        DarkMode.Auto to R.string.follow_system_android_10,
        DarkMode.ForceLight to R.string.always_light,
        DarkMode.ForceDark to R.string.always_dark,
      )

    AlertDialog(
      onDismissRequest = { showDarkModeDialog = false },
      title = { Text(text = stringResource(R.string.dark_mode)) },
      text = {
        Column {
          darkModeItems.forEach { (value, textRes) ->
            Row(
              modifier =
                Modifier.fillMaxWidth().clickable {
                  showDarkModeDialog = false
                  onDarkModeChange(value)
                },
              verticalAlignment = Alignment.CenterVertically,
            ) {
              RadioButton(selected = uiState.darkMode == value, onClick = null)
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
        TextButton(onClick = { showDarkModeDialog = false }) {
          Text(text = stringResource(R.string.ok))
        }
      },
    )
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
