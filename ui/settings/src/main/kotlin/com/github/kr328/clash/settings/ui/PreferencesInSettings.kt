// TODO: https://github.com/zhanghai/ComposePreference/pull/34
@file:Suppress("PackageDirectoryMismatch", "NOTHING_TO_INLINE")

package me.zhanghai.compose.preference

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString

internal inline fun <T> LazyListScope.listPreference(
  key: String,
  value: T,
  noinline onValueChange: (T) -> Unit,
  values: List<T>,
  noinline title: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  noinline icon: @Composable (() -> Unit)? = null,
  noinline summary: @Composable (() -> Unit)? = null,
  type: ListPreferenceType = ListPreferenceType.ALERT_DIALOG,
  noinline valueToText: @Composable (T) -> AnnotatedString = { AnnotatedString(it.toString()) },
) {
  item(key = key, contentType = "ListPreference") {
    ListPreference(
      value = value,
      onValueChange = onValueChange,
      values = values,
      title = title,
      modifier = modifier,
      enabled = enabled,
      icon = icon,
      summary = summary,
      type = type,
      valueToText = valueToText,
    )
  }
}

internal inline fun LazyListScope.switchPreference(
  key: String,
  value: Boolean,
  noinline onValueChange: (Boolean) -> Unit,
  noinline title: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  noinline icon: @Composable (() -> Unit)? = null,
  noinline summary: @Composable (() -> Unit)? = null,
) {
  item(key = key, contentType = "SwitchPreference") {
    SwitchPreference(
      value = value,
      onValueChange = onValueChange,
      title = title,
      modifier = modifier,
      enabled = enabled,
      icon = icon,
      summary = summary,
    )
  }
}
