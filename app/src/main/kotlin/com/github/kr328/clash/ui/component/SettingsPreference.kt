package com.github.kr328.clash.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.github.kr328.clash.R
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.Preference

@Composable
fun <T> SettingsListPreferenceItem(
  value: T,
  values: List<T>,
  @StringRes title: Int,
  @StringRes summary: Int,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  valueToText: @Composable (T) -> Int,
  onValueChange: (T) -> Unit,
) {
  ListPreference(
    value = value,
    onValueChange = onValueChange,
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
  values: List<String>?,
  onClick: () -> Unit,
  enabled: Boolean = true,
) {
  Preference(
    modifier = Modifier.fillMaxWidth(),
    title = { Text(stringResource(title)) },
    summary = { Text(values.summary(placeholder)) },
    enabled = enabled,
    onClick = onClick,
  )
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
fun EmptyEditorContent(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
    Text(stringResource(R.string.empty))
  }
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
