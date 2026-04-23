package com.github.kr328.clash.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import com.github.kr328.clash.R
import com.github.kr328.clash.ui.theme.mihomoDimens

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsCommonScreen(
  title: String,
  modifier: Modifier = Modifier,
  snackbarHost: @Composable () -> Unit = {},
  content: @Composable ColumnScope.() -> Unit,
) {
  MihomoScaffold(
    title = title,
    modifier = modifier,
    snackbarHost = snackbarHost,
    scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
  ) { innerPadding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())
    ) {
      content()
    }
  }
}

@Composable
fun SettingsTipsItem(modifier: Modifier = Modifier, text: CharSequence) {
  val dimens = mihomoDimens
  Row(
    modifier = modifier.padding(vertical = dimens.itemPaddingVertical),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(modifier = Modifier.width(dimens.tipsIconMargin))
    Image(
      painter = painterResource(R.drawable.ic_outline_info),
      contentDescription = null,
      modifier = Modifier.size(dimens.tipsIconSize),
    )
    Spacer(modifier = Modifier.width(dimens.tipsIconMargin))
    when (text) {
      is String ->
        Text(
          text = text,
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.padding(end = dimens.tipsIconMargin),
        )
      is AnnotatedString ->
        Text(
          text = text,
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.padding(end = dimens.tipsIconMargin),
        )
    }
  }
}

@Composable
fun SettingsCategoryTitle(text: String, modifier: Modifier = Modifier) {
  val dimens = mihomoDimens
  val headerLayoutWidth = dimens.itemHeaderComponentSize + dimens.itemHeaderMargin * 2

  Text(
    text = text,
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.primary,
    modifier =
      modifier
        .fillMaxWidth()
        .padding(
          start = headerLayoutWidth,
          end = dimens.settingsItemEndPadding,
          top = dimens.itemPaddingVertical,
          bottom = dimens.itemPaddingVertical,
        ),
  )
}

@Composable
fun SettingsClickableItem(
  title: String,
  summary: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val dimens = mihomoDimens
  val headerLayoutWidth = dimens.itemHeaderComponentSize + dimens.itemHeaderMargin * 2

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(
          top = dimens.itemPaddingVertical,
          bottom = dimens.itemPaddingVertical,
          end = dimens.settingsItemEndPadding,
        ),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(modifier = Modifier.width(headerLayoutWidth))
    Column {
      Text(text = title, style = MaterialTheme.typography.bodyLarge)
      Text(
        text = summary,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = dimens.itemTextMargin),
      )
    }
  }
}
