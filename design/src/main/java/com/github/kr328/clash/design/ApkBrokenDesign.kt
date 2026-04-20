package com.github.kr328.clash.design

import android.content.Context
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.github.kr328.clash.design.component.SettingsCategoryTitle
import com.github.kr328.clash.design.component.SettingsClickableItem
import com.github.kr328.clash.design.component.SettingsCommonScreen
import com.github.kr328.clash.design.component.SettingsTipsItem
import com.github.kr328.clash.design.ui.theme.MihomoDesignTheme
import com.github.kr328.clash.design.ui.theme.PreviewMihomo

class ApkBrokenDesign(context: Context) : Design<Any>(context) {
  override val root: View by composeView { MihomoDesignTheme { ApkBrokenScreen() } }
}

@Composable
private fun ApkBrokenScreen() {
  SettingsCommonScreen(title = stringResource(R.string.application_broken)) {
    val context = LocalContext.current

    SettingsTipsItem(text = stringResource(R.string.application_broken_tips))
    SettingsCategoryTitle(text = stringResource(R.string.reinstall))
    SettingsClickableItem(
      title = stringResource(R.string.github_releases),
      summary = CMFA_GITHUB,
      onClick = { context.openLink(CMFA_GITHUB) },
    )
  }
}

@PreviewMihomo
@Composable
private fun ApkBrokenScreenPreview() = MihomoDesignTheme { ApkBrokenScreen() }
