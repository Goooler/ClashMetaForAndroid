package com.github.kr328.clash.design.component

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ModelProgressBarConfigure {
  var isIndeterminate: Boolean
  var text: String?
  var progress: Int
  var max: Int
}

interface ModelProgressBarScope {
  suspend fun configure(block: suspend ModelProgressBarConfigure.() -> Unit)
}

class ModelProgressBarState {
  var visible by mutableStateOf(false)
  var isIndeterminate by mutableStateOf(true)
  var text by mutableStateOf<String?>(null)
  var progress by mutableIntStateOf(0)
  var max by mutableIntStateOf(0)
}

suspend fun ModelProgressBarState.withModelProgressBar(
  block: suspend ModelProgressBarScope.() -> Unit
) {
  withContext(Dispatchers.Main) {
    visible = true
    isIndeterminate = true
    progress = 0
    max = 0
  }

  val configureImpl =
    object : ModelProgressBarConfigure {
      override var isIndeterminate: Boolean
        get() = this@withModelProgressBar.isIndeterminate
        set(value) {
          this@withModelProgressBar.isIndeterminate = value
        }

      override var text: String?
        get() = this@withModelProgressBar.text
        set(value) {
          this@withModelProgressBar.text = value
        }

      override var progress: Int
        get() = this@withModelProgressBar.progress
        set(value) {
          this@withModelProgressBar.progress = value
        }

      override var max: Int
        get() = this@withModelProgressBar.max
        set(value) {
          this@withModelProgressBar.max = value
        }
    }

  val scopeImpl =
    object : ModelProgressBarScope {
      override suspend fun configure(block: suspend ModelProgressBarConfigure.() -> Unit) {
        withContext(Dispatchers.Main) { configureImpl.block() }
      }
    }

  try {
    scopeImpl.block()
  } finally {
    withContext(Dispatchers.Main) {
      visible = false
      text = null
    }
  }
}

@Composable
fun ModelProgressBarDialog(state: ModelProgressBarState) {
  if (!state.visible) return

  Dialog(onDismissRequest = {}) {
    Surface(shape = MaterialTheme.shapes.large) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(text = state.text.orEmpty(), style = MaterialTheme.typography.bodyLarge)

        if (state.isIndeterminate) {
          CircularProgressIndicator()
        } else {
          val coercedMax = state.max.coerceAtLeast(1)
          LinearProgressIndicator(
            progress = { state.progress.toFloat() / coercedMax.toFloat() },
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    }
  }
}
