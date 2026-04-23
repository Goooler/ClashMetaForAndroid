package com.github.kr328.clash.main.ui

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.core.net.toUri
import com.github.kr328.clash.R
import com.github.kr328.clash.ui.component.SettingsCategoryTitle
import com.github.kr328.clash.ui.component.SettingsClickableItem
import com.github.kr328.clash.ui.component.SettingsCommonScreen
import com.github.kr328.clash.ui.component.SettingsTipsItem
import com.github.kr328.clash.ui.theme.MihomoTheme
import com.github.kr328.clash.ui.theme.PreviewMihomo

@Composable
fun HelpScreen(modifier: Modifier = Modifier) {
  SettingsCommonScreen(title = stringResource(R.string.help), modifier = modifier) {
    val context = LocalContext.current
    SettingsTipsItem(text = AnnotatedString.fromHtml(stringResource(R.string.tips_help)))
    SettingsCategoryTitle(text = stringResource(R.string.document))
    SettingsClickableItem(
      title = stringResource(R.string.clash_wiki),
      summary = CLASH_WIKI,
      onClick = { context.openLink(CLASH_WIKI) },
    )
    SettingsClickableItem(
      title = stringResource(R.string.clash_meta_wiki),
      summary = CLASH_META_WIKI,
      onClick = { context.openLink(CLASH_META_WIKI) },
    )
    SettingsCategoryTitle(text = stringResource(R.string.sources))
    SettingsClickableItem(
      title = stringResource(R.string.clash_meta_core),
      summary = CLASH_META_CORE,
      onClick = { context.openLink(CLASH_META_CORE) },
    )
    SettingsClickableItem(
      title = stringResource(R.string.clash_meta_for_android),
      summary = CMFA_GITHUB,
      onClick = { context.openLink(CMFA_GITHUB) },
    )
  }
}

private const val CLASH_WIKI = "https://github.com/Dreamacro/clash/wiki"
private const val CLASH_META_WIKI = "https://docs.metacubex.one/"
private const val CLASH_META_CORE = "https://github.com/MetaCubeX/Clash.Meta"
internal const val CMFA_GITHUB = "https://github.com/MetaCubeX/ClashMetaForAndroid"

internal fun Context.openLink(link: String) {
  startActivity(Intent(Intent.ACTION_VIEW).setData(link.toUri()))
}

@PreviewMihomo @Composable private fun HelpScreenPreview() = MihomoTheme { HelpScreen() }
