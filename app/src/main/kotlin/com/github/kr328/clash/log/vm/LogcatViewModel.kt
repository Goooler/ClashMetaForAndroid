package com.github.kr328.clash.log.vm

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.R
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.log.LogcatService
import com.github.kr328.clash.log.util.LogcatFilter
import com.github.kr328.clash.log.util.LogcatReader
import com.github.kr328.clash.model.LogFile
import com.github.kr328.clash.util.logsDir
import java.io.OutputStreamWriter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class LogcatViewModel(app: Application) : AndroidViewModel(app), DefaultLifecycleObserver {
  private var conn: ServiceConnection? = null
  @SuppressLint("StaticFieldLeak") private var logcat: LogcatService? = null
  private var pollJob: Job? = null
  private var currentFile: LogFile? = null
  private var initialized = false
  private var started = false
  private var initialSnapshot = true

  val uiState: StateFlow<UiState>
    field = MutableStateFlow(UiState())

  val eventState: StateFlow<EventState>
    field = MutableStateFlow<EventState>(EventState.Idle)

  fun init(fileName: String?) {
    if (initialized) return
    initialized = true

    val file = fileName?.let(LogFile::parseFromFileName)

    if (fileName != null && file == null) {
      eventState.value = EventState.InvalidFile
      return
    }

    if (file != null) {
      currentFile = file
      uiState.update { it.copy(streaming = false) }
      loadLocalFile(file)
      return
    }

    uiState.update { it.copy(streaming = true) }
    startStreaming()
  }

  fun close() {
    eventState.value =
      if (uiState.value.streaming) {
        application.stopService(LogcatService::class.intent)
        EventState.OpenLogs
      } else {
        EventState.Close
      }
  }

  fun delete() {
    val file = currentFile ?: return

    viewModelScope.launch {
      withContext(Dispatchers.IO) { application.logsDir.resolve(file.fileName).delete() }
      eventState.value = EventState.Close
    }
  }

  fun requestExport() {
    val file = currentFile ?: return
    eventState.value = EventState.RequestExport(file.fileName)
  }

  fun exportTo(uri: Uri?) {
    val file = currentFile ?: return
    if (uri == null) return

    viewModelScope.launch {
      val messages = uiState.value.messages

      eventState.value =
        try {
          writeLogTo(messages, file, uri)
          EventState.ShowMessage(application.getString(R.string.file_exported))
        } catch (e: Exception) {
          EventState.ShowMessage(e.message ?: application.getString(R.string.unknown))
        }
    }
  }

  fun consumeEvent() {
    eventState.value = EventState.Idle
  }

  override fun onStart(owner: LifecycleOwner) {
    started = true
  }

  override fun onStop(owner: LifecycleOwner) {
    started = false
  }

  override fun onCleared() {
    pollJob?.cancel()
    conn?.let { connection -> runCatching { application.unbindService(connection) } }
    conn = null
    logcat = null

    super.onCleared()
  }

  private fun loadLocalFile(file: LogFile) {
    viewModelScope.launch {
      val messages =
        try {
          LogcatReader(application, file).readAll()
        } catch (e: Exception) {
          Log.e("Fail to read log file ${file.fileName}: ${e.message}", e)
          eventState.value = EventState.InvalidFile
          return@launch
        }

      uiState.update { it.copy(messages = messages) }
    }
  }

  private fun startStreaming() {
    application.startForegroundService(LogcatService::class.intent)

    viewModelScope.launch {
      try {
        logcat = bindLogcatService()
        startPolling()
      } catch (e: Exception) {
        eventState.value =
          EventState.ShowMessage(e.message ?: application.getString(R.string.unknown))
      }
    }
  }

  private fun startPolling() {
    pollJob?.cancel()
    pollJob = viewModelScope.launch {
      while (isActive) {
        if (started) {
          val snapshot = logcat?.snapshot(initialSnapshot)
          if (snapshot != null) {
            uiState.update { it.copy(messages = snapshot.messages) }
            initialSnapshot = false
          }
        }
        delay(500.milliseconds)
      }
    }
  }

  private suspend fun bindLogcatService(): LogcatService {
    return suspendCancellableCoroutine { continuation ->
      val connection =
        object : ServiceConnection {
          override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service ?: return
            val srv = binder.queryLocalInterface("") as LogcatService

            conn = this
            continuation.resume(srv)
          }

          override fun onServiceDisconnected(name: ComponentName?) {
            if (conn === this) {
              conn = null
            }

            logcat = null
          }
        }

      val bound =
        application.bindService(
          LogcatService::class.intent,
          connection,
          Application.BIND_AUTO_CREATE,
        )

      if (!bound) {
        continuation.resumeWithException(IllegalStateException("Failed to bind logcat service"))
        return@suspendCancellableCoroutine
      }

      continuation.invokeOnCancellation {
        if (conn === connection) {
          runCatching { application.unbindService(connection) }
          conn = null
        }
      }
    }
  }

  private suspend fun writeLogTo(messages: List<LogMessage>, file: LogFile, uri: Uri) =
    withContext(Dispatchers.IO) {
      LogcatFilter(
          OutputStreamWriter(requireNotNull(application.contentResolver.openOutputStream(uri))),
          application,
        )
        .use { filter ->
          uiState.update {
            it.copy(
              exportProgress =
                ExportProgress(
                  visible = true,
                  isIndeterminate = true,
                  progress = 0,
                  max = messages.size,
                )
            )
          }

          try {
            filter.writeHeader(file.date)

            messages.forEachIndexed { index, message ->
              uiState.update {
                it.copy(
                  exportProgress =
                    it.exportProgress.copy(isIndeterminate = false, progress = index + 1)
                )
              }

              filter.writeMessage(message)
            }
          } finally {
            uiState.update { it.copy(exportProgress = ExportProgress()) }
          }
        }
    }

  data class UiState(
    val streaming: Boolean = true,
    val messages: List<LogMessage> = emptyList(),
    val exportProgress: ExportProgress = ExportProgress(),
  )

  data class ExportProgress(
    val visible: Boolean = false,
    val isIndeterminate: Boolean = true,
    val text: String? = null,
    val progress: Int = 0,
    val max: Int = 0,
  )

  sealed interface EventState {
    data object Idle : EventState

    data object Close : EventState

    data object InvalidFile : EventState

    data object OpenLogs : EventState

    data class RequestExport(val fileName: String) : EventState

    data class ShowMessage(val message: String) : EventState
  }
}
