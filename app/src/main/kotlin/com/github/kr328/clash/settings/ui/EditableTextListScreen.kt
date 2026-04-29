package com.github.kr328.clash.settings.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.kr328.clash.R
import com.github.kr328.clash.ui.component.EmptyEditorContent
import com.github.kr328.clash.ui.component.MihomoScaffold
import com.github.kr328.clash.ui.component.initialTextFieldValue
import com.github.kr328.clash.ui.icon.BaselineAdd
import com.github.kr328.clash.ui.icon.MihomoIcons
import com.github.kr328.clash.ui.icon.OutlineDelete
import com.github.kr328.clash.ui.theme.MihomoTheme
import com.github.kr328.clash.ui.theme.PreviewMihomo
import com.github.kr328.clash.ui.theme.mihomoDimens
import kotlinx.serialization.Serializable

@Serializable data class EditableTextList(val title: Int, val initialValues: List<String>?) : NavKey

fun EntryProviderScope<NavKey>.editableTextListScreenEntry(
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
@OptIn(ExperimentalMaterial3Api::class)
fun EditableTextListScreen(
  @StringRes title: Int,
  initialValues: List<String>?,
  onDismiss: () -> Unit,
  onApply: (List<String>?) -> Unit,
) {
  val values = remember(initialValues) { initialValues.orEmpty().toMutableStateList() }
  var showAddDialog by remember { mutableStateOf(false) }

  MihomoScaffold(
    title = stringResource(title),
    onBack = onDismiss,
    actions = {
      IconButton(onClick = { showAddDialog = true }) {
        Icon(
          imageVector = MihomoIcons.BaselineAdd,
          contentDescription = stringResource(R.string._new),
        )
      }
    },
  ) { innerPadding ->
    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      if (values.isEmpty()) {
        EmptyEditorContent(Modifier.weight(1f))
      } else {
        LazyColumn(modifier = Modifier.weight(1f)) {
          itemsIndexed(values) { index, value ->
            ListItem(
              headlineContent = { Text(value) },
              trailingContent = {
                IconButton(onClick = { values.removeAt(index) }) {
                  Icon(
                    imageVector = MihomoIcons.OutlineDelete,
                    contentDescription = stringResource(R.string.delete),
                  )
                }
              },
            )
            HorizontalDivider()
          }
        }
      }

      val dimens = mihomoDimens
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .padding(
              horizontal = dimens.preferenceDialogButtonBarHorizontalPadding,
              vertical = dimens.preferenceDialogButtonBarVerticalPadding,
            ),
        horizontalArrangement = Arrangement.End,
      ) {
        TextButton(onClick = { onApply(null) }) { Text(stringResource(R.string.reset)) }
        TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        TextButton(onClick = { onApply(values.toList()) }) { Text(stringResource(R.string.ok)) }
      }
    }
  }

  if (showAddDialog) {
    SingleTextInputDialog(
      title = title,
      onDismiss = { showAddDialog = false },
      onConfirm = { newValue ->
        if (newValue.isNotBlank()) {
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
      TextButton(onClick = { onConfirm(inputText.text) }) { Text(stringResource(R.string.ok)) }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
  )
}

@PreviewMihomo
@Composable
private fun EditableTextListScreenPreview() = MihomoTheme {
  EditableTextListScreen(
    title = R.string.sniff_http_ports,
    initialValues = listOf("80", "8080"),
    onDismiss = {},
    onApply = {},
  )
}
