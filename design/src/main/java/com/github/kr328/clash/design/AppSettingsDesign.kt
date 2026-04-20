package com.github.kr328.clash.design

import android.content.Context
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.design.component.SettingsCategoryTitle
import com.github.kr328.clash.design.component.SettingsCommonScreen
import com.github.kr328.clash.design.model.Behavior
import com.github.kr328.clash.design.model.DarkMode
import com.github.kr328.clash.design.store.UiStore
import com.github.kr328.clash.design.ui.theme.MihomoTheme
import com.github.kr328.clash.design.ui.theme.PreviewMihomo
import com.github.kr328.clash.service.store.ServiceStore

class AppSettingsDesign(
  context: Context,
  uiStore: UiStore,
  srvStore: ServiceStore,
  behavior: Behavior,
  running: Boolean,
  onHideIconChange: (hide: Boolean) -> Unit,
) : Design<AppSettingsDesign.Request>(context) {
  enum class Request {
    ReCreateAllActivities
  }

  override val root: View by composeView {
    MihomoTheme {
      AppSettingsScreen(
        running = running,
        autoRestartInitial = behavior.autoRestart,
        darkModeInitial = uiStore.darkMode,
        hideAppIconInitial = uiStore.hideAppIcon,
        hideFromRecentsInitial = uiStore.hideFromRecents,
        dynamicNotificationInitial = srvStore.dynamicNotification,
        onAutoRestartChange = { behavior.autoRestart = it },
        onDarkModeChange = {
          uiStore.darkMode = it
          requests.trySend(Request.ReCreateAllActivities)
        },
        onHideAppIconChange = {
          uiStore.hideAppIcon = it
          onHideIconChange(it)
        },
        onHideFromRecentsChange = {
          uiStore.hideFromRecents = it
          requests.trySend(Request.ReCreateAllActivities)
        },
        onDynamicNotificationChange = { srvStore.dynamicNotification = it },
      )
    }
  }
}

@Composable
private fun AppSettingsScreen(
  running: Boolean,
  autoRestartInitial: Boolean,
  darkModeInitial: DarkMode,
  hideAppIconInitial: Boolean,
  hideFromRecentsInitial: Boolean,
  dynamicNotificationInitial: Boolean,
  onAutoRestartChange: (Boolean) -> Unit,
  onDarkModeChange: (DarkMode) -> Unit,
  onHideAppIconChange: (Boolean) -> Unit,
  onHideFromRecentsChange: (Boolean) -> Unit,
  onDynamicNotificationChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  var autoRestart by remember { mutableStateOf(autoRestartInitial) }
  var darkMode by remember { mutableStateOf(darkModeInitial) }
  var hideAppIcon by remember { mutableStateOf(hideAppIconInitial) }
  var hideFromRecents by remember { mutableStateOf(hideFromRecentsInitial) }
  var dynamicNotification by remember { mutableStateOf(dynamicNotificationInitial) }
  var showDarkModeDialog by remember { mutableStateOf(false) }

  SettingsCommonScreen(title = stringResource(R.string.app), modifier = modifier.fillMaxSize()) {
    SettingsCategoryTitle(text = stringResource(R.string.behavior))
    AppSettingsSwitchItem(
      iconRes = R.drawable.ic_baseline_restore,
      titleRes = R.string.auto_restart,
      summaryRes = R.string.allow_clash_auto_restart,
      checked = autoRestart,
      onCheckedChange = {
        autoRestart = it
        onAutoRestartChange(it)
      },
    )

    SettingsCategoryTitle(text = stringResource(R.string.interface_))
    AppSettingsClickableItem(
      iconRes = R.drawable.ic_baseline_brightness_4,
      titleRes = R.string.dark_mode,
      summaryRes = darkMode.summaryRes(),
      onClick = { showDarkModeDialog = true },
    )
    AppSettingsSwitchItem(
      iconRes = R.drawable.ic_baseline_hide,
      titleRes = R.string.hide_app_icon_title,
      summaryRes = R.string.hide_app_icon_desc,
      checked = hideAppIcon,
      onCheckedChange = {
        hideAppIcon = it
        onHideAppIconChange(it)
      },
    )
    AppSettingsSwitchItem(
      iconRes = R.drawable.ic_baseline_stack,
      titleRes = R.string.hide_from_recents_title,
      summaryRes = R.string.hide_from_recents_desc,
      checked = hideFromRecents,
      onCheckedChange = {
        hideFromRecents = it
        onHideFromRecentsChange(it)
      },
    )

    SettingsCategoryTitle(text = stringResource(R.string.service))
    AppSettingsSwitchItem(
      iconRes = R.drawable.ic_baseline_domain,
      titleRes = R.string.show_traffic,
      summaryRes = R.string.show_traffic_summary,
      checked = dynamicNotification,
      enabled = !running,
      onCheckedChange = {
        dynamicNotification = it
        onDynamicNotificationChange(it)
      },
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
                  darkMode = value
                  showDarkModeDialog = false
                  onDarkModeChange(value)
                },
              verticalAlignment = Alignment.CenterVertically,
            ) {
              RadioButton(selected = darkMode == value, onClick = null)
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

@Composable
private fun AppSettingsClickableItem(
  @DrawableRes iconRes: Int,
  @StringRes titleRes: Int,
  @StringRes summaryRes: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = 75.dp)
        .clickable(onClick = onClick)
        .padding(top = 16.dp, bottom = 16.dp, end = 20.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(modifier = Modifier.width(17.5.dp))
    Icon(
      painter = painterResource(iconRes),
      contentDescription = null,
      modifier = Modifier.size(30.dp),
    )
    Spacer(modifier = Modifier.width(17.5.dp))
    Column {
      Text(text = stringResource(titleRes), style = MaterialTheme.typography.bodyLarge)
      Text(
        text = stringResource(summaryRes),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 5.dp),
      )
    }
  }
}

@Composable
private fun AppSettingsSwitchItem(
  @DrawableRes iconRes: Int,
  @StringRes titleRes: Int,
  @StringRes summaryRes: Int,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  Row(
    modifier = modifier.fillMaxWidth().heightIn(min = 75.dp).padding(top = 16.dp, bottom = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(modifier = Modifier.width(17.5.dp))
    Icon(
      painter = painterResource(iconRes),
      contentDescription = null,
      modifier = Modifier.size(30.dp),
      tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
    )
    Spacer(modifier = Modifier.width(17.5.dp))
    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
      Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.bodyLarge,
        color =
          if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
      )
      Text(
        text = stringResource(summaryRes),
        style = MaterialTheme.typography.bodyMedium,
        color =
          if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
          else MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(top = 5.dp),
      )
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      enabled = enabled,
      modifier = Modifier.padding(end = 20.dp),
    )
  }
}

private fun DarkMode.summaryRes(): Int {
  return when (this) {
    DarkMode.Auto -> R.string.follow_system_android_10
    DarkMode.ForceLight -> R.string.always_light
    DarkMode.ForceDark -> R.string.always_dark
  }
}

@PreviewMihomo
@Composable
private fun AppSettingsScreenPreview() {
  MihomoTheme {
    AppSettingsScreen(
      running = false,
      autoRestartInitial = true,
      darkModeInitial = DarkMode.Auto,
      hideAppIconInitial = false,
      hideFromRecentsInitial = false,
      dynamicNotificationInitial = true,
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
    AppSettingsScreen(
      running = true,
      autoRestartInitial = true,
      darkModeInitial = DarkMode.ForceDark,
      hideAppIconInitial = true,
      hideFromRecentsInitial = true,
      dynamicNotificationInitial = true,
      onAutoRestartChange = {},
      onDarkModeChange = {},
      onHideAppIconChange = {},
      onHideFromRecentsChange = {},
      onDynamicNotificationChange = {},
    )
  }
}
