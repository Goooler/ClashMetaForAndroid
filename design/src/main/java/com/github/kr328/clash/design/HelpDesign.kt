package com.github.kr328.clash.design

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.github.kr328.clash.design.component.SettingsCategoryTitle
import com.github.kr328.clash.design.component.SettingsClickableItem
import com.github.kr328.clash.design.component.SettingsCommonScreen
import com.github.kr328.clash.design.component.SettingsTipsItem
import com.github.kr328.clash.design.ui.theme.MihomoDesignTheme
import com.github.kr328.clash.design.ui.theme.PreviewMihomo

class HelpDesign(context: Context) : Design<Unit>(context) {
  private val composeRoot = composeView {
    MihomoDesignTheme { HelpScreen(modifier = Modifier.fillMaxSize()) }
  }

  override val root: View
    get() = composeRoot
}

@Composable
private fun HelpScreen(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val title = (context as? ComponentActivity)?.title?.toString().orEmpty()

  SettingsCommonScreen(title = title, modifier = modifier) {
    SettingsTipsItem(text = AnnotatedString.fromHtml(stringResource(R.string.tips_help)))

    SettingsCategoryTitle(text = stringResource(id = R.string.document))

    SettingsClickableItem(
      title = stringResource(id = R.string.clash_wiki),
      summary = CLASH_WIKI,
      onClick = { context.openLink(CLASH_WIKI) },
    )

    SettingsClickableItem(
      title = stringResource(id = R.string.clash_meta_wiki),
      summary = CLASH_META_WIKI,
      onClick = { context.openLink(CLASH_META_WIKI) },
    )

    SettingsCategoryTitle(text = stringResource(id = R.string.sources))

    SettingsClickableItem(
      title = stringResource(id = R.string.clash_meta_core),
      summary = CLASH_META_CORE,
      onClick = { context.openLink(CLASH_META_CORE) },
    )

    SettingsClickableItem(
      title = stringResource(id = R.string.clash_meta_for_android),
      summary = CMFA_GITHUB,
      onClick = { context.openLink(CMFA_GITHUB) },
    )

    Spacer(modifier = Modifier.height(12.dp))
  }
}

private const val CLASH_WIKI = "https://github.com/Dreamacro/clash/wiki"
private const val CLASH_META_WIKI = "https://docs.metacubex.one/"
private const val CLASH_META_CORE = "https://github.com/MetaCubeX/Clash.Meta"
private const val CMFA_GITHUB = "https://github.com/MetaCubeX/ClashMetaForAndroid"

private fun Context.openLink(link: String) {
  startActivity(Intent(Intent.ACTION_VIEW).setData(link.toUri()))
}

@PreviewMihomo @Composable private fun HelpScreenPreview() = MihomoDesignTheme { HelpScreen() }
