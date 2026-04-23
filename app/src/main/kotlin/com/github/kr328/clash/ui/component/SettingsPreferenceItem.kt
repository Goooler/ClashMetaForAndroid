package com.github.kr328.clash.ui.component

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
import com.github.kr328.clash.ui.theme.mihomoDimens

@Composable
fun SettingsPreferenceClickableItem(
  @StringRes titleRes: Int,
  @StringRes summaryRes: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  @DrawableRes iconRes: Int? = null,
) {
  val dimens = mihomoDimens
  val headerLayoutWidth = dimens.itemHeaderComponentSize + dimens.itemHeaderMargin * 2

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = dimens.itemMinHeight)
        .clickable(enabled = enabled, onClick = onClick)
        .padding(
          top = dimens.itemPaddingVertical,
          bottom = dimens.itemPaddingVertical,
          end = dimens.settingsItemEndPadding,
        ),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (iconRes == null) {
      Spacer(modifier = Modifier.width(headerLayoutWidth))
    } else {
      Spacer(modifier = Modifier.width(dimens.itemHeaderMargin))
      Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        modifier = Modifier.size(dimens.itemHeaderComponentSize),
        tint =
          if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
      )
      Spacer(modifier = Modifier.width(dimens.itemHeaderMargin))
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
        modifier = Modifier.padding(top = dimens.itemTextMargin),
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
  val dimens = mihomoDimens
  val headerLayoutWidth = dimens.itemHeaderComponentSize + dimens.itemHeaderMargin * 2

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = dimens.itemMinHeight)
        .padding(top = dimens.itemPaddingVertical, bottom = dimens.itemPaddingVertical),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (iconRes == null) {
      Spacer(modifier = Modifier.width(headerLayoutWidth))
    } else {
      Spacer(modifier = Modifier.width(dimens.itemHeaderMargin))
      Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        modifier = Modifier.size(dimens.itemHeaderComponentSize),
        tint =
          if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
      )
      Spacer(modifier = Modifier.width(dimens.itemHeaderMargin))
    }

    Column(modifier = Modifier.weight(1f).padding(end = dimens.settingsSwitchContentEndPadding)) {
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
        modifier = Modifier.padding(top = dimens.itemTextMargin),
      )
    }

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      enabled = enabled,
      modifier = Modifier.padding(end = dimens.settingsItemEndPadding),
    )
  }
}
