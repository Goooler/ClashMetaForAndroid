package com.github.kr328.clash.log.ui

import android.content.ClipData
import android.content.ClipboardManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.R
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.log.vm.LogcatViewModel
import com.github.kr328.clash.ui.component.MihomoScaffold
import com.github.kr328.clash.ui.component.ModelProgressBarDialog
import com.github.kr328.clash.ui.component.ModelProgressBarState
import com.github.kr328.clash.ui.theme.MihomoTheme
import com.github.kr328.clash.ui.theme.PreviewMihomo
import com.github.kr328.clash.ui.theme.mihomoDimens
import com.github.kr328.clash.util.format
import com.github.kr328.clash.util.toast
import java.util.Date
import kotlinx.coroutines.launch

@Composable
fun LogcatScreen(
  fileName: String?,
  modifier: Modifier = Modifier,
  viewModel: LogcatViewModel = viewModel(),
  onOpenLogs: () -> Unit,
  onInvalidFile: () -> Unit,
  onClose: () -> Unit,
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val evenState by viewModel.eventState.collectAsStateWithLifecycle()
  val listState = rememberLazyListState()
  val snackbarHostState = remember { SnackbarHostState() }
  val progressBarState = remember { ModelProgressBarState() }
  val scope = rememberCoroutineScope()
  val messageCopied = stringResource(R.string.copied)
  var previousMessageCount by remember { mutableIntStateOf(0) }

  LaunchedEffect(fileName, viewModel) { viewModel.init(fileName) }

  DisposableEffect(lifecycleOwner, viewModel) {
    lifecycleOwner.lifecycle.addObserver(viewModel)
    onDispose { lifecycleOwner.lifecycle.removeObserver(viewModel) }
  }

  val exportLauncher =
    rememberLauncherForActivityResult(CreateDocument("text/plain")) { uri ->
      viewModel.exportTo(uri)
    }

  LaunchedEffect(evenState) {
    when (val event = evenState) {
      LogcatViewModel.EventState.Idle -> Unit
      LogcatViewModel.EventState.Close -> onClose()
      LogcatViewModel.EventState.InvalidFile -> {
        context.toast(R.string.invalid_log_file)
        onInvalidFile()
      }
      LogcatViewModel.EventState.OpenLogs -> onOpenLogs()
      is LogcatViewModel.EventState.RequestExport -> exportLauncher.launch(event.fileName)
      is LogcatViewModel.EventState.ShowMessage -> {
        snackbarHostState.showSnackbar(
          message = event.message,
          withDismissAction = true,
          duration = SnackbarDuration.Short,
        )
      }
    }
    viewModel.consumeEvent()
  }

  LaunchedEffect(uiState.exportProgress) {
    val exportProgress = uiState.exportProgress
    progressBarState.visible = exportProgress.visible
    progressBarState.isIndeterminate = exportProgress.isIndeterminate
    progressBarState.text = exportProgress.text
    progressBarState.progress = exportProgress.progress
    progressBarState.max = exportProgress.max
  }

  LaunchedEffect(uiState.messages.size, uiState.streaming) {
    val shouldAutoFollow = uiState.streaming && (listState.isBottom || previousMessageCount == 0)
    previousMessageCount = uiState.messages.size

    if (shouldAutoFollow && uiState.messages.isNotEmpty()) {
      listState.scrollToItem(uiState.messages.lastIndex)
    }
  }

  LogcatContent(
    modifier = modifier,
    streaming = uiState.streaming,
    messages = uiState.messages,
    listState = listState,
    progressBarState = progressBarState,
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) { Snackbar(it) } },
    onClose = viewModel::close,
    onDelete = viewModel::delete,
    onExport = viewModel::requestExport,
    onCopyMessage = { message ->
      val data = ClipData.newPlainText("log_message", message.message)
      context.getSystemService<ClipboardManager>()?.setPrimaryClip(data)
      scope.launch {
        snackbarHostState.showSnackbar(
          message = messageCopied,
          withDismissAction = true,
          duration = SnackbarDuration.Short,
        )
      }
    },
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogcatContent(
  streaming: Boolean,
  messages: List<LogMessage>,
  listState: LazyListState,
  progressBarState: ModelProgressBarState,
  onClose: () -> Unit,
  onDelete: () -> Unit,
  onExport: () -> Unit,
  onCopyMessage: (LogMessage) -> Unit,
  modifier: Modifier = Modifier,
  snackbarHost: @Composable () -> Unit = {},
) {
  MihomoScaffold(
    title = stringResource(R.string.clash_logcat),
    modifier = modifier,
    snackbarHost = snackbarHost,
    actions = {
      if (streaming) {
        IconButton(onClick = onClose) {
          Icon(
            painter = painterResource(R.drawable.ic_baseline_stop),
            contentDescription = stringResource(R.string.close),
          )
        }
      } else {
        IconButton(onClick = onDelete) {
          Icon(
            painter = painterResource(R.drawable.ic_baseline_delete),
            contentDescription = stringResource(R.string.delete),
          )
        }
        IconButton(onClick = onExport) {
          Icon(
            painter = painterResource(R.drawable.ic_baseline_publish),
            contentDescription = stringResource(R.string.export),
          )
        }
      }
    },
  ) { innerPadding ->
    LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding), state = listState) {
      items(items = messages) { LogcatMessageItem(message = it, onCopyMessage = onCopyMessage) }
    }
  }

  ModelProgressBarDialog(progressBarState)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LogcatMessageItem(message: LogMessage, onCopyMessage: (LogMessage) -> Unit) {
  val context = LocalContext.current
  val dimens = mihomoDimens
  Column(
    modifier =
      Modifier.fillMaxWidth()
        .combinedClickable(onClick = {}, onLongClick = { onCopyMessage(message) })
        .padding(
          horizontal = dimens.logcatPaddingHorizontal,
          vertical = dimens.logcatPaddingVertical,
        ),
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

@PreviewMihomo
@Composable
private fun LogcatContentPreview() = MihomoTheme {
  LogcatContent(
    streaming = false,
    messages =
      listOf(
        LogMessage(LogMessage.Level.Info, "Mihomo started successfully", Date(1710000000000)),
        LogMessage(LogMessage.Level.Warning, "Proxy group fallback in use", Date(1710000005000)),
        LogMessage(LogMessage.Level.Error, "Connection timeout to upstream", Date(1710000010000)),
      ),
    listState = rememberLazyListState(),
    progressBarState = ModelProgressBarState(),
    onClose = {},
    onDelete = {},
    onExport = {},
    onCopyMessage = {},
  )
}
