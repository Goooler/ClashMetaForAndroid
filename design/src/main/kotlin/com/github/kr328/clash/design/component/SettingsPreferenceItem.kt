package com.github.kr328.clash.design.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun SettingsPreferenceClickableItem(
  @StringRes titleRes: Int,
  @StringRes summaryRes: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  @DrawableRes iconRes: Int? = null,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = 75.dp)
        .clickable(enabled = enabled, onClick = onClick)
        .padding(top = 16.dp, bottom = 16.dp, end = 20.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (iconRes == null) {
      Spacer(modifier = Modifier.width(65.dp))
    } else {
      Spacer(modifier = Modifier.width(17.5.dp))
      Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        modifier = Modifier.size(30.dp),
        tint =
          if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
      )
      Spacer(modifier = Modifier.width(17.5.dp))
    }

    Column {
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
  }
}

@Composable
fun SettingsPreferenceSwitchItem(
  @StringRes titleRes: Int,
  @StringRes summaryRes: Int,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  @DrawableRes iconRes: Int? = null,
) {
  Row(
    modifier = modifier.fillMaxWidth().heightIn(min = 75.dp).padding(top = 16.dp, bottom = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (iconRes == null) {
      Spacer(modifier = Modifier.width(65.dp))
    } else {
      Spacer(modifier = Modifier.width(17.5.dp))
      Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        modifier = Modifier.size(30.dp),
        tint =
          if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
      )
      Spacer(modifier = Modifier.width(17.5.dp))
    }

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
