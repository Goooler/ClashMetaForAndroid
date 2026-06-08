package com.github.kr328.clash.log.ui

import android.content.ClipData
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.glue.util.format
import com.github.kr328.clash.log.R
import com.github.kr328.clash.log.vm.LogcatViewModel
import com.github.kr328.clash.ui.component.ModelProgressBarDialog
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineDelete
import com.github.kr328.clash.ui.icon.BaselineSave
import com.github.kr328.clash.ui.icon.BaselineStop
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.lifecycle.withLifecycle
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper
import com.github.kr328.clash.ui.theme.tabbyDimens
import java.util.Date
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun LogcatScreen(
  fileName: String?,
  modifier: Modifier = Modifier,
  viewModel: LogcatViewModel = koinViewModel<LogcatViewModel>().withLifecycle(),
  onOpenLogs: () -> Unit,
  onInvalidFile: () -> Unit,
  onClose: () -> Unit,
) {
  val clipboard = LocalClipboard.current
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val evenState by viewModel.eventState.collectAsStateWithLifecycle()
  val listState = rememberLazyListState()
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val messageCopied = stringResource(R.string.copied)
  val invalidFileTip = stringResource(R.string.invalid_log_file)

  LaunchedEffect(fileName, viewModel) { viewModel.init(fileName) }

  val exportLauncher =
    rememberLauncherForActivityResult(CreateDocument("text/plain")) { uri ->
      viewModel.exportTo(uri)
    }

  LaunchedEffect(evenState) {
    when (val event = evenState) {
      Idle -> Unit
      Close -> onClose()
      InvalidFile -> {
        snackbarHostState.showSnackbar(message = invalidFileTip)
        onInvalidFile()
      }
      OpenLogs -> onOpenLogs()
      is RequestExport -> exportLauncher.launch(event.fileName)
      is ShowMessage -> {
        snackbarHostState.showSnackbar(message = event.message, withDismissAction = true)
      }
    }
    viewModel.consumeEvent()
  }

  LaunchedEffect(listState, uiState.streaming) {
    if (!uiState.streaming) return@LaunchedEffect

    snapshotFlow { uiState.messages.size }
      .collect { size ->
        if (size > 0 && listState.isBottom) {
          listState.animateScrollToItem(size - 1)
        }
      }
  }

  LogcatContent(
    modifier = modifier,
    streaming = uiState.streaming,
    messages = uiState.messages,
    listState = listState,
    snackbarHostState = snackbarHostState,
    onClose = viewModel::close,
    onDelete = viewModel::delete,
    onExport = viewModel::requestExport,
    onCopyMessage = { message ->
      scope.launch {
        val clipEntry = ClipData.newPlainText("log_message", message.message).toClipEntry()
        clipboard.setClipEntry(clipEntry)
        snackbarHostState.showSnackbar(message = messageCopied, withDismissAction = true)
      }
    },
  )

  ModelProgressBarDialog(
    visible = uiState.exportProgress.visible,
    isIndeterminate = uiState.exportProgress.isIndeterminate,
    text = uiState.exportProgress.text,
    progress = uiState.exportProgress.progress,
    max = uiState.exportProgress.max,
  )
}

@Composable
private fun LogcatContent(
  streaming: Boolean,
  messages: List<LogMessage>,
  listState: LazyListState,
  snackbarHostState: SnackbarHostState,
  onClose: () -> Unit,
  onDelete: () -> Unit,
  onExport: () -> Unit,
  onCopyMessage: (LogMessage) -> Unit,
  modifier: Modifier = Modifier,
) {
  TabbyScaffold(
    title = stringResource(R.string.tabby_logcat),
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    actions = {
      if (streaming) {
        IconButton(onClick = onClose) {
          Icon(
            imageVector = TabbyIcons.BaselineStop,
            contentDescription = stringResource(CommonR.string.close),
          )
        }
      } else {
        IconButton(onClick = onDelete) {
          Icon(
            imageVector = TabbyIcons.BaselineDelete,
            contentDescription = stringResource(CommonR.string.delete),
          )
        }
        IconButton(onClick = onExport) {
          Icon(
            imageVector = TabbyIcons.BaselineSave,
            contentDescription = stringResource(CommonR.string.export),
          )
        }
      }
    },
  ) { innerPadding ->
    LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding), state = listState) {
      items(items = messages) { LogcatMessageItem(message = it, onCopyMessage = onCopyMessage) }
    }
  }
}

@Composable
private fun LogcatMessageItem(message: LogMessage, onCopyMessage: (LogMessage) -> Unit) {
  val context = LocalContext.current
  val dimens = tabbyDimens
  Column(
    modifier =
      Modifier.fillMaxWidth()
        .combinedClickable(onClick = {}, onLongClick = { onCopyMessage(message) })
        .padding(12.dp),
    verticalArrangement = Arrangement.Center,
  ) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = message.level.name,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
      )
      Text(
        text = message.time.format(context, includeDate = false, includeTime = true),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.weight(1f),
        textAlign = TextAlign.End,
      )
    }
    Text(
      text = message.message,
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier.fillMaxWidth().padding(top = dimens.itemTextMargin),
    )
  }
}

private val LazyListState.isBottom: Boolean
  get() {
    val layoutInfo = layoutInfo
    val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return true

    return lastVisibleItem.index == layoutInfo.totalItemsCount - 1 &&
      lastVisibleItem.offset + lastVisibleItem.size <= layoutInfo.viewportEndOffset
  }

@PreviewWrapper(TabbyThemeWrapper::class)
@PreviewTabby
@Composable
private fun LogcatContentPreview() {
  LogcatContent(
    streaming = false,
    messages =
      listOf(
        LogMessage(LogMessage.Level.Info, "Tabby started successfully", Date(1710000000000)),
        LogMessage(LogMessage.Level.Warning, "Proxy group fallback in use", Date(1710000005000)),
        LogMessage(LogMessage.Level.Error, "Connection timeout to upstream", Date(1710000010000)),
      ),
    listState = rememberLazyListState(),
    snackbarHostState = SnackbarHostState(),
    onClose = {},
    onDelete = {},
    onExport = {},
    onCopyMessage = {},
  )
}
