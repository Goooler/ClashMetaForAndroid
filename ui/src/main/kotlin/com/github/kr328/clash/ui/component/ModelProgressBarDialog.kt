package com.github.kr328.clash.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.github.kr328.clash.ui.theme.mihomoDimens

@Composable
fun ModelProgressBarDialog(
  visible: Boolean,
  isIndeterminate: Boolean,
  text: String?,
  progress: Int,
  max: Int,
) {
  if (!visible) return

  val dimens = mihomoDimens

  Dialog(onDismissRequest = {}) {
    Surface(shape = MaterialTheme.shapes.large) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(dimens.dialogPadding),
        verticalArrangement = Arrangement.spacedBy(dimens.dialogContentSpacing),
      ) {
        Text(text = text.orEmpty(), style = MaterialTheme.typography.bodyLarge)

        if (isIndeterminate) {
          CircularProgressIndicator()
        } else {
          val coercedMax = max.coerceAtLeast(1)
          val coercedProgress = progress.coerceIn(0, coercedMax)
          LinearProgressIndicator(
            progress = { coercedProgress.toFloat() / coercedMax.toFloat() },
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    }
  }
}
