package com.github.kr328.clash.home.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.glue.util.CLASH_META_CORE
import com.github.kr328.clash.glue.util.CLASH_META_WIKI
import com.github.kr328.clash.glue.util.CLASH_WIKI
import com.github.kr328.clash.glue.util.TABBY_GITHUB
import com.github.kr328.clash.glue.util.openLink
import com.github.kr328.clash.home.R
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.OutlineInfo
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory

@Composable
internal fun HelpScreen(modifier: Modifier = Modifier) {
  TabbyScaffold(title = stringResource(R.string.help), modifier = modifier) { innerPadding ->
    val context = LocalContext.current
    ProvidePreferenceLocals {
      LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        preference(
          key = "tips_help",
          title = {},
          icon = { Icon(imageVector = TabbyIcons.OutlineInfo, contentDescription = null) },
          summary = { Text(AnnotatedString.fromHtml(stringResource(R.string.tips_help))) },
          enabled = false,
        )
        preferenceCategory(
          key = "cat_document",
          title = { Text(stringResource(R.string.document)) },
        )
        preference(
          key = "clash_wiki",
          title = { Text(stringResource(R.string.clash_wiki)) },
          summary = { Text(CLASH_WIKI) },
          onClick = { context.openLink(CLASH_WIKI) },
        )
        preference(
          key = "clash_meta_wiki",
          title = { Text(stringResource(R.string.clash_meta_wiki)) },
          summary = { Text(CLASH_META_WIKI) },
          onClick = { context.openLink(CLASH_META_WIKI) },
        )
        preferenceCategory(key = "cat_sources", title = { Text(stringResource(R.string.sources)) })
        preference(
          key = "clash_meta_core",
          title = { Text(stringResource(R.string.clash_meta_core)) },
          summary = { Text(CLASH_META_CORE) },
          onClick = { context.openLink(CLASH_META_CORE) },
        )
        preference(
          key = "clash_meta_for_android",
          title = { Text(stringResource(CommonR.string.tabby)) },
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
private fun HelpScreenPreview() {
  HelpScreen()
}
