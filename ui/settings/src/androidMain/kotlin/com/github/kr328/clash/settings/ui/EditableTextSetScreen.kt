package com.github.kr328.clash.settings.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.kr328.clash.common.Res as CommonRes
import com.github.kr328.clash.common._new
import com.github.kr328.clash.common.cancel
import com.github.kr328.clash.common.delete
import com.github.kr328.clash.common.ok
import com.github.kr328.clash.common.reorder
import com.github.kr328.clash.common.reset
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineAdd
import com.github.kr328.clash.ui.icon.BaselineDragHandle
import com.github.kr328.clash.ui.icon.OutlineDelete
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Serializable
internal data class EditableTextList(val titleKey: String, val initialValues: Set<String>?) : NavKey

internal fun EntryProviderScope<NavKey>.editableTextSetScreenEntry(
  onDismiss: () -> Unit,
  onApply: (Set<String>?) -> Unit,
) {
  entry<EditableTextList> { key ->
    EditableTextSetScreen(
      titleKey = key.titleKey,
      initialValues = key.initialValues,
      onDismiss = onDismiss,
      onApply = onApply,
    )
  }
}

@Composable
private fun EditableTextSetScreen(
  titleKey: String,
  initialValues: Set<String>?,
  onDismiss: () -> Unit,
  onApply: (Set<String>?) -> Unit,
) {
  val context = LocalContext.current
  val id =
    remember(titleKey) {
      context.resources.getIdentifier(titleKey, "string", context.packageName)
    }
  val title = androidx.compose.ui.res.stringResource(id)

  val values = remember(initialValues) { initialValues.orEmpty().toMutableStateList() }
  var showAddDialog by remember { mutableStateOf(false) }
  var editingValue by remember { mutableStateOf<String?>(null) }
  val lazyListState = rememberLazyListState()
  val reorderableLazyListState =
    rememberReorderableLazyListState(lazyListState) { from, to ->
      values.apply { add(to.index, removeAt(from.index)) }
    }

  TabbyScaffold(
    title = title,
    onBack = onDismiss,
    actions = {
      IconButton(onClick = { showAddDialog = true }) {
        Icon(
          imageVector = TabbyIcons.BaselineAdd,
          contentDescription = stringResource(CommonRes.string._new),
        )
      }
    },
  ) { innerPadding ->
    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      if (values.isEmpty()) {
        EmptyEditorContent(Modifier.weight(1f))
      } else {
        LazyColumn(state = lazyListState, modifier = Modifier.weight(1f)) {
          items(items = values, key = { it }) { value ->
            ReorderableItem(reorderableLazyListState, key = value) { _ ->
              ListItem(
                headlineContent = { Text(value) },
                modifier =
                  Modifier.clickable {
                    editingValue = value
                    showAddDialog = true
                  },
                leadingContent = {
                  Icon(
                    imageVector = TabbyIcons.BaselineDragHandle,
                    contentDescription = stringResource(CommonRes.string.reorder),
                    modifier = Modifier.draggableHandle(),
                  )
                },
                trailingContent = {
                  IconButton(onClick = { values.remove(value) }) {
                    Icon(
                      imageVector = TabbyIcons.OutlineDelete,
                      contentDescription = stringResource(CommonRes.string.delete),
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
        TextButton(onClick = { onApply(null) }) { Text(stringResource(CommonRes.string.reset)) }
        TextButton(onClick = onDismiss) { Text(stringResource(CommonRes.string.cancel)) }
        TextButton(onClick = { onApply(values.toSet()) }) {
          Text(stringResource(CommonRes.string.ok))
        }
      }
    }
  }

  if (showAddDialog) {
    SingleTextInputDialog(
      title = title,
      initialText = editingValue.orEmpty(),
      onDismiss = {
        showAddDialog = false
        editingValue = null
      },
      onConfirm = { newValue ->
        val normalizedValue = newValue.trim()

        if (normalizedValue.isNotBlank()) {
          if (editingValue == null) {
            if (!values.contains(normalizedValue)) {
              values.add(normalizedValue)
            }
          } else if (!values.contains(normalizedValue) || editingValue == normalizedValue) {
            val idx = values.indexOf(editingValue)
            if (idx >= 0) values[idx] = normalizedValue
          }
        }

        showAddDialog = false
        editingValue = null
      },
    )
  }
}

@Composable
private fun SingleTextInputDialog(
  title: String,
  initialText: String,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit,
) {
  var inputText by remember(initialText) { mutableStateOf(initialTextFieldValue(initialText)) }
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
    keyboardController?.show()
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
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
        Text(stringResource(CommonRes.string.ok))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(CommonRes.string.cancel)) }
    },
  )
}

@PreviewWrapper(TabbyThemeWrapper::class)
@PreviewTabby
@Composable
private fun EditableTextSetScreenPreview() {
  EditableTextSetScreen(
    titleKey = "sniff_http_ports",
    initialValues = setOf("80", "8080"),
    onDismiss = {},
    onApply = {},
  )
}
