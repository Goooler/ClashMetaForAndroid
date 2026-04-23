package com.github.kr328.clash.log.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.R
import com.github.kr328.clash.log.vm.LogsViewModel
import com.github.kr328.clash.model.LogFile
import com.github.kr328.clash.ui.component.MihomoScaffold
import com.github.kr328.clash.ui.theme.MihomoTheme
import com.github.kr328.clash.ui.theme.PreviewMihomo
import com.github.kr328.clash.ui.theme.mihomoDimens
import com.github.kr328.clash.util.format
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
  modifier: Modifier = Modifier,
  viewModel: LogsViewModel = viewModel(),
  onStartLogcat: () -> Unit,
  onOpenFile: (LogFile) -> Unit,
) {
  var showDeleteAllDialog by remember { mutableStateOf(false) }
  if (showDeleteAllDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteAllDialog = false },
      title = { Text(text = stringResource(R.string.delete_all_logs)) },
      text = { Text(text = stringResource(R.string.delete_all_logs_warn)) },
      confirmButton = {
        TextButton(
          onClick = {
            showDeleteAllDialog = false
            viewModel.deleteAll()
          }
        ) {
          Text(text = stringResource(R.string.ok))
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteAllDialog = false }) {
          Text(text = stringResource(R.string.cancel))
        }
      },
    )
  }

  LaunchedEffect(viewModel) { viewModel.init() }
  val logs by viewModel.logFiles.collectAsStateWithLifecycle()

  LogsContent(
    modifier = modifier,
    logs = logs,
    onDeleteAllConfirm = { showDeleteAllDialog = true },
    onStartLogcat = onStartLogcat,
    onOpenFile = onOpenFile,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogsContent(
  modifier: Modifier = Modifier,
  logs: List<LogFile>,
  onDeleteAllConfirm: () -> Unit,
  onStartLogcat: () -> Unit,
  onOpenFile: (LogFile) -> Unit,
) {
  MihomoScaffold(
    modifier = modifier,
    title = stringResource(R.string.logs),
    actions = {
      IconButton(onClick = onDeleteAllConfirm) {
        Icon(
          painter = painterResource(R.drawable.ic_baseline_clear_all),
          contentDescription = stringResource(R.string.delete_all_logs),
        )
      }
    },
  ) { innerPadding ->
    val context = LocalContext.current
    val dimens = mihomoDimens

    LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      item {
        LogsActionItem(
          title = stringResource(R.string.clash_logcat),
          summary = stringResource(R.string.tap_to_start),
          icon = R.drawable.ic_baseline_adb,
          onClick = onStartLogcat,
        )
      }
      item { HorizontalDivider() }
      item {
        Text(
          text = stringResource(R.string.history),
          color = MaterialTheme.colorScheme.primary,
          modifier =
            Modifier.fillMaxWidth()
              .padding(
                start = dimens.itemHeaderComponentSize + dimens.itemHeaderMargin * 2,
                end = dimens.settingsItemEndPadding,
                top = dimens.itemPaddingVertical,
                bottom = dimens.itemPaddingVertical,
              ),
        )
      }
      items(items = logs, key = LogFile::fileName) { file ->
        LogsActionItem(
          title = file.fileName,
          summary = file.date.format(context),
          onClick = { onOpenFile(file) },
        )
      }
    }
  }
}

@Composable
private fun LogsActionItem(
  title: String,
  summary: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  @DrawableRes icon: Int? = null,
) {
  val dimens = mihomoDimens
  val headerLayoutWidth = dimens.itemHeaderComponentSize + dimens.itemHeaderMargin * 2
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(end = dimens.settingsItemEndPadding),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier.size(width = headerLayoutWidth, height = dimens.itemMinHeight),
      contentAlignment = Alignment.Center,
    ) {
      if (icon != null) {
        Icon(
          painter = painterResource(icon),
          contentDescription = null,
          modifier = Modifier.size(dimens.itemHeaderComponentSize),
        )
      }
    }

    Column(
      modifier = Modifier.heightIn(min = dimens.itemMinHeight),
      verticalArrangement = Arrangement.Center,
    ) {
      Text(text = title)
      Spacer(modifier = Modifier.size(dimens.itemTextMargin))
      Text(text = summary, style = MaterialTheme.typography.bodyMedium)
    }
  }
}

@PreviewMihomo
@Composable
private fun LogsScreenPreview() = MihomoTheme {
  LogsContent(
    logs =
      listOf(
        LogFile("clash-1710000000000.log", Date(1710000000000)),
        LogFile("clash-1710000000001.log", Date(1710000000001)),
      ),
    onDeleteAllConfirm = {},
    onStartLogcat = {},
    onOpenFile = {},
  )
}
