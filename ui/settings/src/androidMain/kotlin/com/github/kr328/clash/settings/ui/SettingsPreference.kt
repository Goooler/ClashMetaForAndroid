package com.github.kr328.clash.settings.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.settings.Res
import com.github.kr328.clash.settings.empty
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EmptyEditorContent(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
    Text(stringResource(Res.string.empty))
  }
}

@Composable
internal fun List<String>?.listSummary(placeholder: Any) =
  when {
    this == null -> stringResource(placeholder)
    isEmpty() -> stringResource(Res.string.empty)
    else -> stringResource(CommonR.string.format_elements, size)
  }

internal fun initialTextFieldValue(text: String) =
  TextFieldValue(text = text, selection = TextRange(text.length))

@Composable
internal fun stringResource(res: Any, vararg formatArgs: Any): String {
  return when (res) {
    is org.jetbrains.compose.resources.StringResource ->
      org.jetbrains.compose.resources.stringResource(res, *formatArgs)
    is Int -> androidx.compose.ui.res.stringResource(res, *formatArgs)
    else -> throw IllegalArgumentException("Unsupported resource type: $res")
  }
}

internal fun android.content.Context.getStringId(res: Any): Int {
  return when (res) {
    is Int -> res
    is org.jetbrains.compose.resources.StringResource -> {
      val id = resources.getIdentifier(res.key, "string", packageName)
      if (id == 0) {
        throw IllegalArgumentException("Resource not found for key: ${res.key}")
      }
      id
    }
    else -> throw IllegalArgumentException("Unsupported resource type: $res")
  }
}
