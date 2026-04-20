package com.github.kr328.clash.design

import android.content.Context
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.design.component.MihomoScaffold
import com.github.kr328.clash.design.ui.theme.MihomoDesignTheme
import com.github.kr328.clash.design.ui.theme.PreviewMihomo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppCrashedDesign(context: Context) : Design<Unit>(context) {
  private var logs by mutableStateOf("")

  override val root: View by composeView { MihomoDesignTheme { AppCrashedScreen(logs = logs) } }

  suspend fun updateLogs(logs: String) =
    withContext(Dispatchers.Main) { this@AppCrashedDesign.logs = logs }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppCrashedScreen(logs: String) {
  MihomoScaffold(title = stringResource(R.string.application_crashed)) { innerPadding ->
    SelectionContainer {
      Text(
        text = logs,
        style =
          TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5,
          ),
        modifier =
          Modifier.fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
      )
    }
  }
}

@PreviewMihomo
@Composable
private fun AppCrashedScreenPreview() {
  MihomoDesignTheme {
    AppCrashedScreen(
      logs =
        "04-20 10:10:10.000 I/App( 1234): App version: 2.0.0\n" +
          "04-20 10:10:10.100 E/App( 1234): java.lang.IllegalStateException: Example crash\n" +
          "    at com.example.app.MainActivity.onCreate(MainActivity.kt:42)\n" +
          "    at android.app.Activity.performCreate(Activity.java:9000)"
    )
  }
}
