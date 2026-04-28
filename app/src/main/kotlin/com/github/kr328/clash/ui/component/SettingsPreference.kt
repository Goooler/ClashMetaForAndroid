package com.github.kr328.clash.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.kr328.clash.R
import com.github.kr328.clash.ui.icon.BaselineAdd
import com.github.kr328.clash.ui.icon.MihomoIcons
import com.github.kr328.clash.ui.theme.mihomoDimens
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
  val dimens = mihomoDimens

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
            imageVector = MihomoIcons.BaselineAdd,
            contentDescription = stringResource(R.string._new),
          )
        }
      },
    ) { innerPadding ->
      Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        content(Modifier.weight(1f))
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .padding(
                horizontal = dimens.preferenceDialogButtonBarHorizontalPadding,
                vertical = dimens.preferenceDialogButtonBarVerticalPadding,
              ),
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

fun initialTextFieldValue(text: String) =
  TextFieldValue(text = text, selection = TextRange(text.length))

@Composable
private fun List<String>?.summary(@StringRes placeholder: Int) =
  when {
    this == null -> stringResource(placeholder)
    isEmpty() -> stringResource(R.string.empty)
    else -> stringResource(R.string.format_elements, size)
  }
