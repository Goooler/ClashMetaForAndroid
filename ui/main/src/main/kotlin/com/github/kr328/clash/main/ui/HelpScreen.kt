package com.github.kr328.clash.main.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.github.kr328.clash.glue.R
import com.github.kr328.clash.ui.component.MihomoScaffold
import com.github.kr328.clash.ui.theme.MihomoThemeWrapper
import com.github.kr328.clash.ui.theme.PreviewMihomo
import com.github.kr328.clash.util.CLASH_META_CORE
import com.github.kr328.clash.util.CLASH_META_WIKI
import com.github.kr328.clash.util.CLASH_WIKI
import com.github.kr328.clash.util.CMFA_GITHUB
import com.github.kr328.clash.util.openLink
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory

@Composable
fun HelpScreen(modifier: Modifier = Modifier) {
  MihomoScaffold(title = stringResource(R.string.help), modifier = modifier) { innerPadding ->
    ProvidePreferenceLocals {
      val context = LocalContext.current
      LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        preference(
          key = "tips_help",
          modifier = Modifier.fillMaxWidth(),
          title = { Text(stringResource(R.string.help)) },
          summary = { Text(AnnotatedString.fromHtml(stringResource(R.string.tips_help))) },
          enabled = false,
        )
        preferenceCategory(
          key = "cat_document",
          title = { Text(stringResource(R.string.document)) },
        )
        preference(
          key = "clash_wiki",
          modifier = Modifier.fillMaxWidth(),
          title = { Text(stringResource(R.string.clash_wiki)) },
          summary = { Text(CLASH_WIKI) },
          onClick = { context.openLink(CLASH_WIKI) },
        )
        preference(
          key = "clash_meta_wiki",
          modifier = Modifier.fillMaxWidth(),
          title = { Text(stringResource(R.string.clash_meta_wiki)) },
          summary = { Text(CLASH_META_WIKI) },
          onClick = { context.openLink(CLASH_META_WIKI) },
        )
        preferenceCategory(key = "cat_sources", title = { Text(stringResource(R.string.sources)) })
        preference(
          key = "clash_meta_core",
          modifier = Modifier.fillMaxWidth(),
          title = { Text(stringResource(R.string.clash_meta_core)) },
          summary = { Text(CLASH_META_CORE) },
          onClick = { context.openLink(CLASH_META_CORE) },
        )
        preference(
          key = "clash_meta_for_android",
          modifier = Modifier.fillMaxWidth(),
          title = { Text(stringResource(R.string.clash_meta_for_android)) },
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
private fun HelpScreenPreview() {
  HelpScreen()
}
