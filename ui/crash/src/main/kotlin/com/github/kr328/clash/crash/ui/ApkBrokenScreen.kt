package com.github.kr328.clash.crash.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.github.kr328.clash.crash.R
import com.github.kr328.clash.glue.util.TABBY_GITHUB
import com.github.kr328.clash.glue.util.openLink
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyTheme
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory

@Composable
internal fun ApkBrokenScreen() {
  TabbyScaffold(title = stringResource(R.string.application_broken)) { innerPadding ->
    val context = LocalContext.current
    ProvidePreferenceLocals {
      LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        preference(
          key = "tips_application_broken",
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
          title = { Text(stringResource(R.string.github_releases)) },
          summary = { Text(TABBY_GITHUB) },
          onClick = { context.openLink(TABBY_GITHUB) },
        )
      }
    }
  }
}

@PreviewTabby @Composable internal fun ApkBrokenScreenPreview() = TabbyTheme { ApkBrokenScreen() }
