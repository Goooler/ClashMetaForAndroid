package com.github.kr328.clash.settings.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.settings.R
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineAdd
import com.github.kr328.clash.ui.icon.BaselineDragHandle
import com.github.kr328.clash.ui.icon.OutlineDelete
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper
import kotlinx.serialization.Serializable
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Serializable
internal data class EditableTextList(val title: Int, val initialValues: List<String>?) : NavKey

internal fun EntryProviderScope<NavKey>.editableTextListScreenEntry(
  onDismiss: () -> Unit,
  onApply: (List<String>?) -> Unit,
) {
  entry<EditableTextList> { key ->
    EditableTextListScreen(
      title = key.title,
      initialValues = key.initialValues,
      onDismiss = onDismiss,
      onApply = onApply,
    )
  }
}

@Composable
private fun EditableTextListScreen(
  @StringRes title: Int,
  initialValues: List<String>?,
  onDismiss: () -> Unit,
  onApply: (List<String>?) -> Unit,
) {
  val values = remember(initialValues) { initialValues.orEmpty().distinct().toMutableStateList() }
  var showAddDialog by remember { mutableStateOf(false) }
  val lazyListState = rememberLazyListState()
  val reorderableLazyListState =
    rememberReorderableLazyListState(lazyListState) { from, to ->
      values.apply { add(to.index, removeAt(from.index)) }
    }

  TabbyScaffold(
    title = stringResource(title),
    onBack = onDismiss,
    actions = {
      IconButton(onClick = { showAddDialog = true }) {
        Icon(
          imageVector = TabbyIcons.BaselineAdd,
          contentDescription = stringResource(CommonR.string._new),
        )
      }
    },
  ) { innerPadding ->
    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      if (values.isEmpty()) {
        EmptyEditorContent(Modifier.weight(1f))
      } else {
        LazyColumn(state = lazyListState, modifier = Modifier.weight(1f)) {
          items(values, key = { it }) { item ->
            ReorderableItem(reorderableLazyListState, key = item) { _ ->
              ListItem(
                headlineContent = { Text(item) },
                leadingContent = {
                  Icon(
                    imageVector = TabbyIcons.BaselineDragHandle,
                    contentDescription = stringResource(CommonR.string.reorder),
                    modifier = Modifier.draggableHandle(),
                  )
                },
                trailingContent = {
                  IconButton(onClick = { values.remove(item) }) {
                    Icon(
                      imageVector = TabbyIcons.OutlineDelete,
                      contentDescription = stringResource(CommonR.string.delete),
                    )
                  }
                },
              )
              HorizontalDivider()
            }
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.End,
      ) {
        TextButton(onClick = { onApply(null) }) { Text(stringResource(CommonR.string.reset)) }
        TextButton(onClick = onDismiss) { Text(stringResource(CommonR.string.cancel)) }
        TextButton(onClick = { onApply(values.toList()) }) {
          Text(stringResource(CommonR.string.ok))
        }
      }
    }
  }

  if (showAddDialog) {
    SingleTextInputDialog(
      title = title,
      onDismiss = { showAddDialog = false },
      onConfirm = { newValue ->
        if (newValue.isNotBlank() && newValue !in values) {
          values.add(newValue)
        }
        showAddDialog = false
      },
    )
  }
}

@Composable
private fun SingleTextInputDialog(
  @StringRes title: Int,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit,
) {
  var inputText by remember { mutableStateOf(initialTextFieldValue("")) }
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
    keyboardController?.show()
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(title)) },
    text = {
      OutlinedTextField(
        value = inputText,
        onValueChange = { inputText = it },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
      )
    },
    confirmButton = {
      TextButton(onClick = { onConfirm(inputText.text) }) {
        Text(stringResource(CommonR.string.ok))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(CommonR.string.cancel)) }
    },
  )
}

@PreviewWrapper(TabbyThemeWrapper::class)
@PreviewTabby
@Composable
private fun EditableTextListScreenPreview() {
  EditableTextListScreen(
    title = R.string.sniff_http_ports,
    initialValues = listOf("80", "8080"),
    onDismiss = {},
    onApply = {},
  )
}
