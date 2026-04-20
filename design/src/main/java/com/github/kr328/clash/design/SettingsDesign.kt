package com.github.kr328.clash.design

import android.content.Context
import android.view.View
import androidx.annotation.DrawableRes
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
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.design.component.SettingsCommonScreen
import com.github.kr328.clash.design.ui.theme.MihomoTheme
import com.github.kr328.clash.design.ui.theme.PreviewMihomo

class SettingsDesign(context: Context) : Design<SettingsDesign.Request>(context) {
  enum class Request {
    StartApp,
    StartNetwork,
    StartOverride,
    StartMetaFeature,
  }

  override val root: View by composeView {
    MihomoTheme { SettingsScreen(onRequest = { requests.trySend(it) }) }
  }
}

@Composable
private fun SettingsScreen(
  modifier: Modifier = Modifier,
  onRequest: (SettingsDesign.Request) -> Unit,
) {
  SettingsCommonScreen(
    title = stringResource(R.string.settings),
    modifier = modifier.fillMaxSize(),
  ) {
    SettingsEntryItem(
      icon = R.drawable.ic_baseline_settings,
      title = stringResource(R.string.app),
      onClick = { onRequest(SettingsDesign.Request.StartApp) },
    )

    SettingsEntryItem(
      icon = R.drawable.ic_baseline_dns,
      title = stringResource(R.string.network),
      onClick = { onRequest(SettingsDesign.Request.StartNetwork) },
    )

    SettingsEntryItem(
      icon = R.drawable.ic_baseline_extension,
      title = stringResource(R.string.override),
      onClick = { onRequest(SettingsDesign.Request.StartOverride) },
    )

    SettingsEntryItem(
      icon = R.drawable.ic_baseline_meta,
      title = stringResource(R.string.meta_features),
      onClick = { onRequest(SettingsDesign.Request.StartMetaFeature) },
    )
  }
}

@Composable
private fun SettingsEntryItem(
  @DrawableRes icon: Int,
  title: String,
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
      painter = painterResource(icon),
      contentDescription = null,
      modifier = Modifier.size(30.dp),
    )
    Spacer(modifier = Modifier.width(17.5.dp))
    Text(text = title, style = MaterialTheme.typography.bodyLarge)
  }
}

@PreviewMihomo
@Composable
private fun SettingsScreenPreview() = MihomoTheme { SettingsScreen(onRequest = {}) }
