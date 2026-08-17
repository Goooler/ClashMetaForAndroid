package com.github.kr328.clash.crash.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.github.kr328.clash.common.util.TABBY_GITHUB
import com.github.kr328.clash.common.util.openLink
import com.github.kr328.clash.crash.R
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.OutlineInfo
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper
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
          key = "tips",
          title = {},
          summary = { Text(stringResource(R.string.application_broken_tips)) },
          icon = { Icon(imageVector = TabbyIcons.OutlineInfo, contentDescription = null) },
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

@PreviewWrapper(TabbyThemeWrapper::class)
@PreviewTabby
@Composable
private fun ApkBrokenScreenPreview() {
  ApkBrokenScreen()
}
