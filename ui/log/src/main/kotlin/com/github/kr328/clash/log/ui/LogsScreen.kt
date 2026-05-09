package com.github.kr328.clash.log.ui

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.glue.util.format
import com.github.kr328.clash.log.R
import com.github.kr328.clash.log.model.LogFile
import com.github.kr328.clash.log.vm.LogsViewModel
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineAdb
import com.github.kr328.clash.ui.icon.BaselineClearAll
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyTheme
import com.github.kr328.clash.ui.theme.tabbyDimens
import java.util.Date

@Composable
internal fun LogsScreen(
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
          Text(text = stringResource(CommonR.string.ok))
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteAllDialog = false }) {
          Text(text = stringResource(CommonR.string.cancel))
        }
      },
    )
  }

  val logs by viewModel.logFiles.collectAsStateWithLifecycle()

  LogsContent(
    modifier = modifier,
    logs = logs,
    onDeleteAllConfirm = { showDeleteAllDialog = true },
    onStartLogcat = onStartLogcat,
    onOpenFile = onOpenFile,
  )
}

@Composable
private fun LogsContent(
  modifier: Modifier = Modifier,
  logs: List<LogFile>,
  onDeleteAllConfirm: () -> Unit,
  onStartLogcat: () -> Unit,
  onOpenFile: (LogFile) -> Unit,
) {
  TabbyScaffold(
    modifier = modifier,
    title = stringResource(CommonR.string.logs),
    actions = {
      IconButton(onClick = onDeleteAllConfirm) {
        Icon(
          imageVector = TabbyIcons.BaselineClearAll,
          contentDescription = stringResource(R.string.delete_all_logs),
        )
      }
    },
  ) { innerPadding ->
    val context = LocalContext.current
    val dimens = tabbyDimens

    LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      item {
        LogsActionItem(
          title = stringResource(R.string.tabby_logcat),
          summary = stringResource(CommonR.string.tap_to_start),
          icon = TabbyIcons.BaselineAdb,
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
          summary = Date(file.created).format(context),
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
  icon: ImageVector? = null,
) {
  val dimens = tabbyDimens
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
          imageVector = icon,
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

@PreviewTabby
@Composable
internal fun LogsScreenPreview() = TabbyTheme {
  LogsContent(
    logs =
      listOf(
        LogFile("clash-1710000000000.log", 1710000000000),
        LogFile("clash-1710000000001.log", 1710000000001),
      ),
    onDeleteAllConfirm = {},
    onStartLogcat = {},
    onOpenFile = {},
  )
}
