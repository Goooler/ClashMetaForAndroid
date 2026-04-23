package com.github.kr328.clash.settings.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.github.kr328.clash.R
import com.github.kr328.clash.ui.component.SettingsCommonScreen
import com.github.kr328.clash.ui.theme.MihomoTheme
import com.github.kr328.clash.ui.theme.PreviewMihomo
import com.github.kr328.clash.ui.theme.mihomoDimens

sealed interface SettingsRoute {
  data object App : SettingsRoute

  data object Network : SettingsRoute

  data object Override : SettingsRoute

  data object MetaFeature : SettingsRoute
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, onRoute: (SettingsRoute) -> Unit) {
  SettingsCommonScreen(
    title = stringResource(R.string.settings),
    modifier = modifier.fillMaxSize(),
  ) {
    SettingsEntryItem(
      iconRes = R.drawable.ic_baseline_settings,
      titleRes = R.string.app,
      onClick = { onRoute(SettingsRoute.App) },
    )

    SettingsEntryItem(
      iconRes = R.drawable.ic_baseline_dns,
      titleRes = R.string.network,
      onClick = { onRoute(SettingsRoute.Network) },
    )

    SettingsEntryItem(
      iconRes = R.drawable.ic_baseline_extension,
      titleRes = R.string.override,
      onClick = { onRoute(SettingsRoute.Override) },
    )

    SettingsEntryItem(
      iconRes = R.drawable.ic_baseline_meta,
      titleRes = R.string.meta_features,
      onClick = { onRoute(SettingsRoute.MetaFeature) },
    )
  }
}

@Composable
private fun SettingsEntryItem(
  @DrawableRes iconRes: Int,
  @StringRes titleRes: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val dimens = mihomoDimens
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = dimens.itemMinHeight)
        .clickable(onClick = onClick)
        .padding(
          top = dimens.itemPaddingVertical,
          bottom = dimens.itemPaddingVertical,
          end = dimens.settingsItemEndPadding,
        ),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(modifier = Modifier.width(dimens.itemHeaderMargin))
    Icon(
      painter = painterResource(iconRes),
      contentDescription = null,
      modifier = Modifier.size(dimens.itemHeaderComponentSize),
    )
    Spacer(modifier = Modifier.width(dimens.itemHeaderMargin))
    Text(text = stringResource(titleRes), style = MaterialTheme.typography.bodyLarge)
  }
}

@PreviewMihomo
@Composable
private fun SettingsScreenPreview() = MihomoTheme { SettingsScreen(onRoute = {}) }
