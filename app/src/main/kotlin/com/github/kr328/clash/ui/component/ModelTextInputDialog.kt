package com.github.kr328.clash.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.github.kr328.clash.R
import com.github.kr328.clash.util.Validator

@Composable
fun ModelTextInputDialog(
  title: String,
  initialValue: String? = null,
  hint: String? = null,
  error: String? = null,
  validator: Validator = { true },
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit,
) {
  val initialText = initialValue.orEmpty()

  var inputText by remember {
    mutableStateOf(
      TextFieldValue(text = initialText, selection = TextRange(initialValue?.length ?: 0))
    )
  }
  var inputError by remember { mutableStateOf(if (!validator(initialText)) error else null) }
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
    keyboardController?.show()
  }

  val isValidInput = validator(inputText.text)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      OutlinedTextField(
        value = inputText,
        onValueChange = { newValue ->
          inputText = newValue
          inputError =
            if (!validator(newValue.text)) {
              error
            } else {
              null
            }
        },
        label = hint?.let { { Text(it) } },
        isError = inputError != null,
        supportingText = inputError?.let { { Text(it) } },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
      )
    },
    confirmButton = {
      TextButton(onClick = { onConfirm(inputText.text) }, enabled = isValidInput) {
        Text(stringResource(R.string.ok))
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
  )
}
