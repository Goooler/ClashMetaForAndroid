package com.github.kr328.clash.design

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.design.component.MihomoScaffold
import com.github.kr328.clash.design.ui.ToastDuration
import com.github.kr328.clash.design.ui.theme.MihomoDesignTheme
import com.github.kr328.clash.design.ui.theme.PreviewMihomo
import com.github.kr328.clash.design.util.format
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogcatDesign(context: Context, private val streaming: Boolean) :
  Design<LogcatDesign.Request>(context) {
  enum class Request {
    Close,
    Delete,
    Export,
  }

  private var messages by mutableStateOf<List<LogMessage>>(emptyList())
  private val listState = LazyListState()

  private val onCopyMessage: (LogMessage) -> Unit = {
    launch {
      val data = ClipData.newPlainText("log_message", it.message)
      context.getSystemService<ClipboardManager>()?.setPrimaryClip(data)
      showToast(R.string.copied, ToastDuration.Short)
    }
  }

  override val root: View by composeView {
    MihomoDesignTheme {
      LogcatScreen(
        title = stringResource(R.string.clash_logcat),
        streaming = streaming,
        messages = messages,
        listState = listState,
        onClose = { requests.trySend(Request.Close) },
        onDelete = { requests.trySend(Request.Delete) },
        onExport = { requests.trySend(Request.Export) },
        onCopyMessage = onCopyMessage,
      )
    }
  }

  suspend fun patchMessages(messages: List<LogMessage>) =
    withContext(Dispatchers.Main) {
      this@LogcatDesign.messages = messages

      if (streaming && listState.isTop && messages.isNotEmpty()) {
        listState.scrollToItem(messages.lastIndex)
      }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogcatScreen(
  title: String,
  streaming: Boolean,
  messages: List<LogMessage>,
  listState: LazyListState,
  onClose: () -> Unit,
  onDelete: () -> Unit,
  onExport: () -> Unit,
  onCopyMessage: (LogMessage) -> Unit,
) {
  MihomoScaffold(
    title = title,
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LogcatMessageItem(message: LogMessage, onCopyMessage: (LogMessage) -> Unit) {
  val context = LocalContext.current
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
      modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
    )
  }
}

private val LazyListState.isTop: Boolean
  get() = firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0

@PreviewMihomo
@Composable
private fun LogcatScreenPreview() = MihomoDesignTheme {
  LogcatScreen(
    title = stringResource(R.string.clash_logcat),
    streaming = false,
    messages =
      listOf(
        LogMessage(LogMessage.Level.Info, "Mihomo started successfully", Date(1710000000000)),
        LogMessage(LogMessage.Level.Warning, "Proxy group fallback in use", Date(1710000005000)),
        LogMessage(LogMessage.Level.Error, "Connection timeout to upstream", Date(1710000010000)),
      ),
    listState = rememberLazyListState(),
    onClose = {},
    onDelete = {},
    onExport = {},
    onCopyMessage = {},
  )
}
