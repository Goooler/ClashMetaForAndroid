package com.github.kr328.clash.settings.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.github.kr328.clash.common.Res as CommonRes
import com.github.kr328.clash.common.format_elements
import com.github.kr328.clash.settings.Res
import com.github.kr328.clash.settings.empty
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EmptyEditorContent(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
    Text(stringResource(Res.string.empty))
  }
}

@Composable
internal fun List<String>?.listSummary(placeholder: StringResource) =
  when {
    this == null -> stringResource(placeholder)
    isEmpty() -> stringResource(Res.string.empty)
    else -> stringResource(CommonRes.string.format_elements, size)
  }

internal fun initialTextFieldValue(text: String) =
  TextFieldValue(text = text, selection = TextRange(text.length))
