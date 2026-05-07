// TODO: https://github.com/zhanghai/ComposePreference/pull/34
@file:Suppress("PackageDirectoryMismatch", "NOTHING_TO_INLINE")

package me.zhanghai.compose.preference

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

internal inline fun <T> LazyListScope.textFieldPreference(
  key: String,
  value: T,
  noinline onValueChange: (T) -> Unit,
  noinline title: @Composable () -> Unit,
  noinline textToValue: (String) -> T?,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  noinline icon: @Composable (() -> Unit)? = null,
  noinline summary: @Composable (() -> Unit)? = null,
  noinline valueToText: (T) -> String = { it.toString() },
) {
  item(key = key, contentType = "TextFieldPreference") {
    TextFieldPreference(
      value = value,
      onValueChange = onValueChange,
      title = title,
      textToValue = textToValue,
      modifier = modifier,
      enabled = enabled,
      icon = icon,
      summary = summary,
      valueToText = valueToText,
    )
  }
}
