package com.github.kr328.clash.log.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.glue.util.logsDir
import com.github.kr328.clash.log.model.LogFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class LogsViewModel(app: Application) : AndroidViewModel(app) {
  private var initialized = false

  val logFiles: StateFlow<List<LogFile>>
    field = MutableStateFlow(emptyList<LogFile>())

  fun initialize() {
    if (initialized) return
    initialized = true

    viewModelScope.launch { logFiles.value = loadAllLogs() }
  }

  fun deleteAll() {
    viewModelScope.launch {
      deleteAllLogs()
      logFiles.value = loadAllLogs()
    }
  }

  private suspend fun loadAllLogs(): List<LogFile> =
    withContext(Dispatchers.IO) {
      application.logsDir.listFiles()?.toList().orEmpty().mapNotNull { LogFile.parse(it.name) }
    }

  private suspend fun deleteAllLogs() =
    withContext(Dispatchers.IO) { application.logsDir.deleteRecursively() }
}
