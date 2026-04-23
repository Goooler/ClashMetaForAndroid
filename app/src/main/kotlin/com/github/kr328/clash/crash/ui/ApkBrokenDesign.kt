package com.github.kr328.clash.crash.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.github.kr328.clash.R
import com.github.kr328.clash.main.ui.CMFA_GITHUB
import com.github.kr328.clash.main.ui.openLink
import com.github.kr328.clash.ui.Design
import com.github.kr328.clash.ui.component.SettingsCategoryTitle
import com.github.kr328.clash.ui.component.SettingsClickableItem
import com.github.kr328.clash.ui.component.SettingsCommonScreen
import com.github.kr328.clash.ui.component.SettingsTipsItem
import com.github.kr328.clash.ui.theme.MihomoTheme
import com.github.kr328.clash.ui.theme.PreviewMihomo

class ApkBrokenDesign(context: Context) : Design<Unit>(context) {
  @Composable override fun Content() = MihomoTheme { ApkBrokenScreen() }
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

@PreviewMihomo @Composable private fun ApkBrokenScreenPreview() = MihomoTheme { ApkBrokenScreen() }
