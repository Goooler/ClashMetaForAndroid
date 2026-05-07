package com.github.kr328.clash.crash.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.github.kr328.clash.crash.R
import com.github.kr328.clash.ui.component.MihomoScaffold
import com.github.kr328.clash.ui.theme.MihomoThemeWrapper
import com.github.kr328.clash.ui.theme.PreviewMihomo
import com.github.kr328.clash.util.CMFA_GITHUB
import com.github.kr328.clash.util.openLink
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory

@Composable
internal fun ApkBrokenScreen() {
  MihomoScaffold(title = stringResource(R.string.application_broken)) { innerPadding ->
    ProvidePreferenceLocals {
      val context = LocalContext.current
      LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        preference(
          key = "tips_application_broken",
          modifier = Modifier.fillMaxWidth(),
          title = { Text(stringResource(R.string.application_broken)) },
          summary = { Text(stringResource(R.string.application_broken_tips)) },
          enabled = false,
        )
        preferenceCategory(
          key = "cat_reinstall",
          title = { Text(stringResource(R.string.reinstall)) },
        )
        preference(
          key = "github_releases",
          modifier = Modifier.fillMaxWidth(),
          title = { Text(stringResource(R.string.github_releases)) },
          summary = { Text(CMFA_GITHUB) },
          onClick = { context.openLink(CMFA_GITHUB) },
        )
      }
    }
  }
}

@PreviewWrapper(MihomoThemeWrapper::class)
@PreviewMihomo
@Composable
private fun ApkBrokenScreenPreview() {
  ApkBrokenScreen()
}
