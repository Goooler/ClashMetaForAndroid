package com.github.kr328.clash.home.ui

import android.content.ClipData
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.common.Res as CommonRes
import com.github.kr328.clash.common.copied
import com.github.kr328.clash.common.tabby
import com.github.kr328.clash.glue.util.MIHOMO_CORE
import com.github.kr328.clash.glue.util.MIHOMO_WIKI
import com.github.kr328.clash.glue.util.TABBY_GITHUB
import com.github.kr328.clash.glue.util.openLink
import com.github.kr328.clash.home.Res
import com.github.kr328.clash.home.about
import com.github.kr328.clash.home.app_version
import com.github.kr328.clash.home.check_for_updates
import com.github.kr328.clash.home.core_version
import com.github.kr328.clash.home.document
import com.github.kr328.clash.home.help
import com.github.kr328.clash.home.mihomo_core
import com.github.kr328.clash.home.mihomo_wiki
import com.github.kr328.clash.home.open
import com.github.kr328.clash.home.sources
import com.github.kr328.clash.home.tips_help
import com.github.kr328.clash.home.update_available
import com.github.kr328.clash.home.vm.HelpViewModel
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineMihomo
import com.github.kr328.clash.ui.icon.BaselineUpdate
import com.github.kr328.clash.ui.icon.OutlineInfo
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper
import kotlinx.coroutines.launch
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HelpScreen(modifier: Modifier = Modifier, viewModel: HelpViewModel = koinViewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val context = LocalContext.current
  val updateAvailableText = stringResource(Res.string.update_available)
  val openActionText = stringResource(Res.string.open)

  LaunchedEffect(viewModel) {
    viewModel.eventState.collect { event ->
      when (event) {
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
    }
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
  val clipboard = LocalClipboard.current
  val scope = rememberCoroutineScope()
  val messageCopied = stringResource(CommonRes.string.copied)

  val onCopyVersion: (String) -> Unit = { version ->
    scope.launch {
      val clipEntry = ClipData.newPlainText("version", version).toClipEntry()
      clipboard.setClipEntry(clipEntry)
      snackbarHostState?.showSnackbar(message = messageCopied, withDismissAction = true)
    }
  }

  TabbyScaffold(
    title = stringResource(Res.string.help),
    modifier = modifier,
    snackbarHostState = snackbarHostState,
  ) { innerPadding ->
    ProvidePreferenceLocals {
      LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        preference(
          key = "tips",
          title = {},
          summary = { Text(AnnotatedString.fromHtml(stringResource(Res.string.tips_help))) },
          icon = { Icon(imageVector = TabbyIcons.OutlineInfo, contentDescription = null) },
        )
        preferenceCategory(
          key = "cat_document",
          title = { Text(stringResource(Res.string.document)) },
        )
        preference(
          key = "mihomo_wiki",
          title = { Text(stringResource(Res.string.mihomo_wiki)) },
          summary = { Text(MIHOMO_WIKI) },
          onClick = { onOpenLink(MIHOMO_WIKI) },
        )
        preferenceCategory(
          key = "cat_sources",
          title = { Text(stringResource(Res.string.sources)) },
        )
        preference(
          key = "mihomo_core",
          title = { Text(stringResource(Res.string.mihomo_core)) },
          summary = { Text(MIHOMO_CORE) },
          onClick = { onOpenLink(MIHOMO_CORE) },
        )
        preference(
          key = "tabby",
          title = { Text(stringResource(CommonRes.string.tabby)) },
          summary = { Text(TABBY_GITHUB) },
          onClick = { onOpenLink(TABBY_GITHUB) },
        )
        preferenceCategory(key = "cat_update", title = { Text(stringResource(Res.string.about)) })
        preference(
          key = "app_version",
          title = { Text(stringResource(Res.string.app_version)) },
          summary = { Text(uiState.appVersion) },
          icon = {
            Icon(
              painter = painterResource(CommonR.drawable.ic_tabby_small),
              contentDescription = null,
            )
          },
          modifier =
            Modifier.combinedClickable(
              onClick = {},
              onLongClick = { onCopyVersion(uiState.appVersion) },
            ),
        )
        preference(
          key = "core_version",
          title = { Text(stringResource(Res.string.core_version)) },
          summary = { Text(uiState.coreVersion) },
          icon = {
            Icon(
              imageVector = TabbyIcons.BaselineMihomo,
              contentDescription = null,
            )
          },
          modifier =
            Modifier.combinedClickable(
              onClick = {},
              onLongClick = { onCopyVersion(uiState.coreVersion) },
            ),
        )
        preference(
          key = "check_for_updates",
          title = { Text(stringResource(Res.string.check_for_updates)) },
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
