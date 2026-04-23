package com.github.kr328.clash.log.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.model.LogFile
import com.github.kr328.clash.util.logsDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogsViewModel(app: Application) : AndroidViewModel(app) {
  private val _logFiles = MutableStateFlow(emptyList<LogFile>())
  val logFiles: StateFlow<List<LogFile>> = _logFiles

  fun init() {
    viewModelScope.launch { _logFiles.value = loadFiles() }
  }

  fun deleteAll() {
    viewModelScope.launch {
      deleteAllLogs()
      _logFiles.value = loadFiles()
    }
  }

  private suspend fun loadFiles(): List<LogFile> =
    withContext(Dispatchers.IO) {
      application.logsDir.listFiles()?.toList().orEmpty().mapNotNull {
        LogFile.parseFromFileName(it.name)
      }
    }

  private suspend fun deleteAllLogs() =
    withContext(Dispatchers.IO) { application.logsDir.deleteRecursively() }
}
