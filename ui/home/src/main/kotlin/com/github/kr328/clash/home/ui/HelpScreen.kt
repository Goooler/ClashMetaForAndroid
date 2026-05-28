package com.github.kr328.clash.home.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.glue.util.CLASH_META_CORE
import com.github.kr328.clash.glue.util.CLASH_META_WIKI
import com.github.kr328.clash.glue.util.CLASH_WIKI
import com.github.kr328.clash.glue.util.TABBY_GITHUB
import com.github.kr328.clash.glue.util.openLink
import com.github.kr328.clash.home.R
import com.github.kr328.clash.home.vm.HelpViewModel
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineInfo
import com.github.kr328.clash.ui.icon.BaselineUpdate
import com.github.kr328.clash.ui.icon.OutlineInfo
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory

@Composable
internal fun HelpScreen(modifier: Modifier = Modifier, viewModel: HelpViewModel = viewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val eventState by viewModel.eventState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val context = LocalContext.current
  val updateAvailableText = stringResource(R.string.update_available)
  val openActionText = stringResource(R.string.open)

  LaunchedEffect(eventState) {
    when (val event = eventState) {
      HelpViewModel.EventState.Idle -> Unit
      is HelpViewModel.EventState.ShowMessage -> {
        snackbarHostState.showSnackbar(message = event.message)
      }
      is HelpViewModel.EventState.UpdateAvailable -> {
        val result =
          snackbarHostState.showSnackbar(
            message = updateAvailableText,
            actionLabel = openActionText,
            duration = SnackbarDuration.Long,
          )
        if (result == SnackbarResult.ActionPerformed) {
          context.openLink(event.releasesUrl)
        }
      }
    }
    viewModel.consumeEvent()
  }

  HelpContent(
    modifier = modifier,
    uiState = uiState,
    snackbarHostState = snackbarHostState,
    onOpenLink = { url -> context.openLink(url) },
    onCheckForUpdates = viewModel::checkForUpdates,
  )
}

@Composable
private fun HelpContent(
  uiState: HelpViewModel.UiState,
  onOpenLink: (String) -> Unit,
  onCheckForUpdates: () -> Unit,
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState? = null,
) {
  TabbyScaffold(
    title = stringResource(R.string.help),
    modifier = modifier,
    snackbarHostState = snackbarHostState,
  ) { innerPadding ->
    ProvidePreferenceLocals {
      LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        preference(
          key = "tips",
          title = {},
          summary = { Text(AnnotatedString.fromHtml(stringResource(R.string.tips_help))) },
          icon = { Icon(imageVector = TabbyIcons.OutlineInfo, contentDescription = null) },
        )
        preferenceCategory(
          key = "cat_document",
          title = { Text(stringResource(R.string.document)) },
        )
        preference(
          key = "clash_wiki",
          title = { Text(stringResource(R.string.clash_wiki)) },
          summary = { Text(CLASH_WIKI) },
          onClick = { onOpenLink(CLASH_WIKI) },
        )
        preference(
          key = "clash_meta_wiki",
          title = { Text(stringResource(R.string.clash_meta_wiki)) },
          summary = { Text(CLASH_META_WIKI) },
          onClick = { onOpenLink(CLASH_META_WIKI) },
        )
        preferenceCategory(key = "cat_sources", title = { Text(stringResource(R.string.sources)) })
        preference(
          key = "clash_meta_core",
          title = { Text(stringResource(R.string.clash_meta_core)) },
          summary = { Text(CLASH_META_CORE) },
          onClick = { onOpenLink(CLASH_META_CORE) },
        )
        preference(
          key = "clash_meta_for_android",
          title = { Text(stringResource(CommonR.string.tabby)) },
          summary = { Text(TABBY_GITHUB) },
          onClick = { onOpenLink(TABBY_GITHUB) },
        )
        preferenceCategory(key = "cat_update", title = { Text(stringResource(R.string.about)) })
        preference(
          key = "app_version",
          title = { Text(stringResource(R.string.app_version)) },
          summary = { Text(uiState.appVersion) },
          icon = { Icon(imageVector = TabbyIcons.BaselineInfo, contentDescription = null) },
        )
        preference(
          key = "kernel_version",
          title = { Text(stringResource(R.string.kernel_version)) },
          summary = { Text(uiState.coreVersion) },
          icon = {
            Icon(
              painter = painterResource(CommonR.drawable.ic_tabby_small),
              contentDescription = null,
            )
          },
        )
        preference(
          key = "check_for_updates",
          title = { Text(stringResource(R.string.check_for_updates)) },
          icon = {
            if (uiState.checkingForUpdates) {
              CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
              Icon(imageVector = TabbyIcons.BaselineUpdate, contentDescription = null)
            }
          },
          onClick = onCheckForUpdates,
        )
      }
    }
  }
}

@PreviewWrapper(TabbyThemeWrapper::class)
@PreviewTabby
@Composable
private fun HelpContentPreview() {
  HelpContent(uiState = HelpViewModel.UiState(), onOpenLink = {}, onCheckForUpdates = {})
}
