package com.github.kr328.clash.settings.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.kr328.clash.glue.model.DarkMode
import com.github.kr328.clash.settings.Res
import com.github.kr328.clash.settings.allow_tabby_auto_restart
import com.github.kr328.clash.settings.always_dark
import com.github.kr328.clash.settings.always_light
import com.github.kr328.clash.settings.app
import com.github.kr328.clash.settings.auto_restart
import com.github.kr328.clash.settings.behavior
import com.github.kr328.clash.settings.dark_mode
import com.github.kr328.clash.settings.follow_system
import com.github.kr328.clash.settings.hide_app_icon_desc
import com.github.kr328.clash.settings.hide_app_icon_title
import com.github.kr328.clash.settings.hide_from_recents_desc
import com.github.kr328.clash.settings.hide_from_recents_title
import com.github.kr328.clash.settings.interface_
import com.github.kr328.clash.settings.service
import com.github.kr328.clash.settings.show_traffic
import com.github.kr328.clash.settings.show_traffic_summary
import com.github.kr328.clash.settings.vm.AppSettingsViewModel
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineBrightness4
import com.github.kr328.clash.ui.icon.BaselineDomain
import com.github.kr328.clash.ui.icon.BaselineHide
import com.github.kr328.clash.ui.icon.BaselineRestore
import com.github.kr328.clash.ui.icon.BaselineStack
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper
import com.github.kr328.clash.ui.util.stringResCompat
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.listPreference
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.switchPreference
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AppSettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: AppSettingsViewModel = koinViewModel(),
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
  TabbyScaffold(title = stringResource(Res.string.app), modifier = modifier) { innerPadding ->
    ProvidePreferenceLocals {
      LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        preferenceCategory(
          key = "cat_behavior",
          title = { Text(stringResource(Res.string.behavior)) },
        )
        switchPreference(
          key = "auto_restart",
          value = uiState.autoRestart,
          onValueChange = onAutoRestartChange,
          icon = { Icon(imageVector = TabbyIcons.BaselineRestore, contentDescription = null) },
          title = { Text(stringResource(Res.string.auto_restart)) },
          summary = { Text(stringResource(Res.string.allow_tabby_auto_restart)) },
        )
        preferenceCategory(
          key = "cat_interface",
          title = { Text(stringResource(Res.string.interface_)) },
        )
        listPreference(
          key = "dark_mode",
          value = uiState.darkMode,
          onValueChange = onDarkModeChange,
          values = listOf(DarkMode.Auto, DarkMode.ForceLight, DarkMode.ForceDark),
          icon = { Icon(imageVector = TabbyIcons.BaselineBrightness4, contentDescription = null) },
          title = { Text(stringResource(Res.string.dark_mode)) },
          summary = { Text(stringResCompat(uiState.darkMode.summaryRes)) },
          valueToText = {
            androidx.compose.ui.text.AnnotatedString(stringResCompat(it.summaryRes))
          },
        )
        switchPreference(
          key = "hide_app_icon",
          value = uiState.hideAppIcon,
          onValueChange = onHideAppIconChange,
          icon = { Icon(imageVector = TabbyIcons.BaselineHide, contentDescription = null) },
          title = { Text(stringResource(Res.string.hide_app_icon_title)) },
          summary = { Text(stringResource(Res.string.hide_app_icon_desc)) },
        )
        switchPreference(
          key = "hide_from_recents",
          value = uiState.hideFromRecents,
          onValueChange = onHideFromRecentsChange,
          icon = { Icon(imageVector = TabbyIcons.BaselineStack, contentDescription = null) },
          title = { Text(stringResource(Res.string.hide_from_recents_title)) },
          summary = { Text(stringResource(Res.string.hide_from_recents_desc)) },
        )
        preferenceCategory(
          key = "cat_service",
          title = { Text(stringResource(Res.string.service)) },
        )
        switchPreference(
          key = "show_traffic",
          value = uiState.dynamicNotification,
          onValueChange = onDynamicNotificationChange,
          enabled = !clashRunning,
          icon = { Icon(imageVector = TabbyIcons.BaselineDomain, contentDescription = null) },
          title = { Text(stringResource(Res.string.show_traffic)) },
          summary = { Text(stringResource(Res.string.show_traffic_summary)) },
        )
      }
    }
  }
}

private val DarkMode.summaryRes: Any
  get() =
    when (this) {
      Auto -> Res.string.follow_system
      ForceLight -> Res.string.always_light
      ForceDark -> Res.string.always_dark
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
    onAutoRestartChange = {},
    onDarkModeChange = {},
    onHideAppIconChange = {},
    onHideFromRecentsChange = {},
    onDynamicNotificationChange = {},
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
    onAutoRestartChange = {},
    onDarkModeChange = {},
    onHideAppIconChange = {},
    onHideFromRecentsChange = {},
    onDynamicNotificationChange = {},
  )
}
