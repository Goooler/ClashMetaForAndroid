package com.github.kr328.clash.settings.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.settings.Res
import com.github.kr328.clash.settings.reset_override_settings
import com.github.kr328.clash.settings.reset_override_settings_message
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ResetOverrideSettingsDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(Res.string.reset_override_settings)) },
    text = { Text(stringResource(Res.string.reset_override_settings_message)) },
    confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(CommonR.string.ok)) } },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(CommonR.string.cancel)) }
    },
  )
}
