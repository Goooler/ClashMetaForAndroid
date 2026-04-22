package com.github.kr328.clash.design.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.kr328.clash.R
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.Preference

@Composable
fun <T> rememberWriteThroughState(initial: T, sync: (T) -> Unit): MutableState<T> = remember {
  object : MutableState<T> {
    private val inner = mutableStateOf(initial)

    override var value: T
      get() = inner.value
      set(v) {
        inner.value = v
        sync(v)
      }

    override fun component1(): T = value

    override fun component2(): (T) -> Unit = { value = it }
  }
}

@Composable
fun <T> SettingsListPreferenceItem(
  state: MutableState<T>,
  values: List<T>,
  @StringRes title: Int,
  @StringRes summary: Int,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  valueToText: @Composable (T) -> Int,
) {
  ListPreference(
    state = state,
    values = values,
    modifier = modifier,
    enabled = enabled,
    title = { Text(stringResource(title)) },
    summary = { Text(stringResource(summary)) },
    valueToText = { AnnotatedString(stringResource(valueToText(it))) },
  )
}

@Composable
fun SettingsEditTextListPreferenceItem(
  @StringRes title: Int,
  @StringRes placeholder: Int,
  state: MutableState<List<String>?>,
  enabled: Boolean = true,
) {
  var values by state
  var showDialog by remember { mutableStateOf(false) }
  Preference(
    modifier = Modifier.fillMaxWidth(),
    title = { Text(stringResource(title)) },
    summary = { Text(values.summary(placeholder)) },
    enabled = enabled,
    onClick = { showDialog = true },
  )
  if (showDialog) {
    EditableTextListDialog(
      title = title,
      initialValues = values,
      onDismiss = { showDialog = false },
      onApply = {
        values = it
        showDialog = false
      },
    )
  }
}

@Composable
fun SettingsClickablePreferenceItem(
  @StringRes title: Int,
  @StringRes summary: Int,
  onClick: () -> Unit,
  enabled: Boolean = true,
) {
  Preference(
    modifier = Modifier.fillMaxWidth(),
    title = { Text(stringResource(title)) },
    summary = { Text(stringResource(summary)) },
    enabled = enabled,
    onClick = onClick,
  )
}

@Composable
fun EditableTextListDialog(
  @StringRes title: Int,
  initialValues: List<String>?,
  onDismiss: () -> Unit,
  onApply: (List<String>?) -> Unit,
) {
  var values by remember(initialValues) { mutableStateOf(initialValues.orEmpty()) }
  var showAddDialog by remember { mutableStateOf(false) }

  FullScreenPreferenceDialog(
    title = title,
    onDismiss = onDismiss,
    onAdd = { showAddDialog = true },
    onReset = { onApply(null) },
    onConfirm = { onApply(values) },
  ) { modifier ->
    if (values.isEmpty()) {
      EmptyEditorContent(modifier)
    } else {
      LazyColumn(modifier = modifier) {
        itemsIndexed(values) { index, value ->
          ListItem(
            headlineContent = { Text(value) },
            trailingContent = {
              IconButton(onClick = { values = values.toMutableList().apply { removeAt(index) } }) {
                Icon(
                  painter = painterResource(R.drawable.ic_outline_delete),
                  contentDescription = stringResource(R.string.delete),
                )
              }
            },
          )
          HorizontalDivider()
        }
      }
    }
  }

  if (showAddDialog) {
    SingleTextInputDialog(
      title = title,
      initialValue = "",
      onDismiss = { showAddDialog = false },
      onConfirm = { newValue ->
        if (newValue.isNotBlank()) {
          values = values + newValue
        }
        showAddDialog = false
      },
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenPreferenceDialog(
  @StringRes title: Int,
  onDismiss: () -> Unit,
  onAdd: () -> Unit,
  onReset: () -> Unit,
  onConfirm: () -> Unit,
  content: @Composable (Modifier) -> Unit,
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    MihomoScaffold(
      modifier = Modifier.fillMaxSize(),
      title = stringResource(title),
      onBack = onDismiss,
      actions = {
        IconButton(onClick = onAdd) {
          Icon(
            painter = painterResource(R.drawable.ic_baseline_add),
            contentDescription = stringResource(R.string._new),
          )
        }
      },
    ) { innerPadding ->
      Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        content(Modifier.weight(1f))
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.End,
        ) {
          TextButton(onClick = onReset) { Text(stringResource(R.string.reset)) }
          TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
          TextButton(onClick = onConfirm) { Text(stringResource(R.string.ok)) }
        }
      }
    }
  }
}

@Composable
fun EmptyEditorContent(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
    Text(stringResource(R.string.empty))
  }
}

@Composable
fun SingleTextInputDialog(
  @StringRes title: Int,
  initialValue: String,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit,
) {
  var inputText by remember { mutableStateOf(initialTextFieldValue(initialValue)) }
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

fun initialTextFieldValue(text: String) =
  TextFieldValue(text = text, selection = TextRange(text.length))

@Composable
private fun List<String>?.summary(@StringRes placeholder: Int) =
  when {
    this == null -> stringResource(placeholder)
    isEmpty() -> stringResource(R.string.empty)
    else -> stringResource(R.string.format_elements, size)
  }
