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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EditableTextMapScreen(
  @StringRes title: Int,
  initialValues: Map<String, String>?,
  onDismiss: () -> Unit,
  onApply: (Map<String, String>?) -> Unit,
) {
  var values by
    remember(initialValues) {
      mutableStateOf(initialValues?.entries?.map { it.toPair() }.orEmpty())
    }
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
          itemsIndexed(values) { index, entry ->
            ListItem(
              headlineContent = { Text(entry.first) },
              supportingContent = { Text(entry.second) },
              trailingContent = {
                IconButton(
                  onClick = { values = values.toMutableList().apply { removeAt(index) } }
                ) {
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
        TextButton(onClick = { onApply(values.toMap()) }) { Text(stringResource(R.string.ok)) }
      }
    }
  }

  if (showAddDialog) {
    MapEntryInputDialog(
      title = title,
      onDismiss = { showAddDialog = false },
      onConfirm = { key, valueText ->
        values = values + (key to valueText)
        showAddDialog = false
      },
    )
  }
}

@Composable
private fun MapEntryInputDialog(
  @StringRes title: Int,
  onDismiss: () -> Unit,
  onConfirm: (String, String) -> Unit,
) {
  var keyText by remember { mutableStateOf(initialTextFieldValue("")) }
  var valueText by remember { mutableStateOf(initialTextFieldValue("")) }
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
  val confirmEnabled = keyText.text.isNotBlank() && valueText.text.isNotBlank()

  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
    keyboardController?.show()
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(title)) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = keyText,
          onValueChange = { keyText = it },
          label = { Text(stringResource(R.string.key)) },
          placeholder = { Text(stringResource(R.string.key)) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        )
        OutlinedTextField(
          value = valueText,
          onValueChange = { valueText = it },
          label = { Text(stringResource(R.string.value)) },
          placeholder = { Text(stringResource(R.string.value)) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = { onConfirm(keyText.text.trim(), valueText.text.trim()) },
        enabled = confirmEnabled,
      ) {
        Text(stringResource(R.string.ok))
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
  )
}

@PreviewMihomo
@Composable
private fun EditableTextMapScreenPreview() = MihomoTheme {
  EditableTextMapScreen(
    title = R.string.hosts,
    initialValues = mapOf("example.com" to "127.0.0.1", "test.com" to "192.168.1.1"),
    onDismiss = {},
    onApply = {},
  )
}
