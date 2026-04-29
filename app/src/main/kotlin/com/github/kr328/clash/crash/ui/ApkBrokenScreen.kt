package com.github.kr328.clash.crash.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.github.kr328.clash.R
import com.github.kr328.clash.main.ui.CMFA_GITHUB
import com.github.kr328.clash.main.ui.openLink
import com.github.kr328.clash.ui.component.MihomoScaffold
import com.github.kr328.clash.ui.component.SettingsCategoryTitle
import com.github.kr328.clash.ui.component.SettingsClickableItem
import com.github.kr328.clash.ui.component.SettingsTipsItem
import com.github.kr328.clash.ui.theme.MihomoTheme
import com.github.kr328.clash.ui.theme.PreviewMihomo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApkBrokenScreen() {
  MihomoScaffold(title = stringResource(R.string.application_broken)) { innerPadding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())
    ) {
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
}

@PreviewMihomo @Composable private fun ApkBrokenScreenPreview() = MihomoTheme { ApkBrokenScreen() }
