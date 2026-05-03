package com.github.kr328.clash.settings.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.github.kr328.clash.glue.R

@Composable
internal fun EmptyEditorContent(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
    Text(stringResource(R.string.empty))
  }
}

@Composable
internal fun List<String>?.listSummary(@StringRes placeholder: Int) =
  when {
    this == null -> stringResource(placeholder)
    isEmpty() -> stringResource(R.string.empty)
    else -> stringResource(R.string.format_elements, size)
  }

internal fun initialTextFieldValue(text: String) =
  TextFieldValue(text = text, selection = TextRange(text.length))
