package com.github.kr328.clash.design

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.design.component.SettingsCategoryTitle
import com.github.kr328.clash.design.component.SettingsClickableItem
import com.github.kr328.clash.design.component.SettingsCommonScreen
import com.github.kr328.clash.design.component.SettingsTipsItem
import com.github.kr328.clash.design.ui.theme.MihomoDesignTheme

class HelpDesign(context: Context, openLink: (Uri) -> Unit) : Design<Unit>(context) {
  private val composeRoot = composeView {
    MihomoDesignTheme { HelpScreen(modifier = Modifier.fillMaxSize(), openLink = openLink) }
  }

  override val root: View
    get() = composeRoot
}

@Composable
private fun HelpScreen(modifier: Modifier = Modifier, openLink: (Uri) -> Unit) {
  val context = LocalContext.current
  val title = (context as? ComponentActivity)?.title?.toString().orEmpty()
  val tipsText =
    remember(context) {
      context.getString(R.string.tips_help).replace("<strong>", "").replace("</strong>", "")
    }

  SettingsCommonScreen(
    title = title,
    onBack = {
      when (context) {
        is ComponentActivity -> context.onBackPressedDispatcher.onBackPressed()
      }
    },
    modifier = modifier,
  ) {
    SettingsTipsItem(text = tipsText)

    SettingsCategoryTitle(text = stringResource(id = R.string.document))

    SettingsClickableItem(
      title = stringResource(id = R.string.clash_wiki),
      summary = stringResource(id = R.string.clash_wiki_url),
      onClick = { openLink(Uri.parse(context.getString(R.string.clash_wiki_url))) },
    )

    SettingsClickableItem(
      title = stringResource(id = R.string.clash_meta_wiki),
      summary = stringResource(id = R.string.clash_meta_wiki_url),
      onClick = { openLink(Uri.parse(context.getString(R.string.clash_meta_wiki_url))) },
    )

    SettingsCategoryTitle(text = stringResource(id = R.string.sources))

    SettingsClickableItem(
      title = stringResource(id = R.string.clash_meta_core),
      summary = stringResource(id = R.string.clash_meta_core_url),
      onClick = { openLink(Uri.parse(context.getString(R.string.clash_meta_core_url))) },
    )

    SettingsClickableItem(
      title = stringResource(id = R.string.clash_meta_for_android),
      summary = stringResource(id = R.string.meta_github_url),
      onClick = { openLink(Uri.parse(context.getString(R.string.meta_github_url))) },
    )

    Spacer(modifier = Modifier.height(12.dp))
  }
}
