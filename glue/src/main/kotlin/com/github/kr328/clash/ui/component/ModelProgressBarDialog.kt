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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.github.kr328.clash.ui.theme.mihomoDimens

class ModelProgressBarState {
  var visible by mutableStateOf(false)
  var isIndeterminate by mutableStateOf(true)
  var text by mutableStateOf<String?>(null)
  var progress by mutableIntStateOf(0)
  var max by mutableIntStateOf(0)
}

@Composable
fun ModelProgressBarDialog(state: ModelProgressBarState) {
  if (!state.visible) return

  val dimens = mihomoDimens

  Dialog(onDismissRequest = {}) {
    Surface(shape = MaterialTheme.shapes.large) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(dimens.dialogPadding),
        verticalArrangement = Arrangement.spacedBy(dimens.dialogContentSpacing),
      ) {
        Text(text = state.text.orEmpty(), style = MaterialTheme.typography.bodyLarge)

        if (state.isIndeterminate) {
          CircularProgressIndicator()
        } else {
          val coercedMax = state.max.coerceAtLeast(1)
          val coercedProgress = state.progress.coerceIn(0, coercedMax)
          LinearProgressIndicator(
            progress = { coercedProgress.toFloat() / coercedMax.toFloat() },
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    }
  }
}
