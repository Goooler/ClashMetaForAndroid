package com.github.kr328.clash.profile.vm

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.model.File
import com.github.kr328.clash.remote.FilesClient
import com.github.kr328.clash.service.model.Profile
import com.github.kr328.clash.util.fileName
import com.github.kr328.clash.util.withProfile
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FilesViewModel(app: Application) : AndroidViewModel(app), DefaultLifecycleObserver {
  private val client = FilesClient(app)
  private val stack = ArrayDeque<String>()
  private var root: String = ""
  private var fetchJob: Job? = null

  val uiState: StateFlow<UiState>
    field = MutableStateFlow(UiState())

  val eventState: StateFlow<EventState>
    field = MutableStateFlow<EventState>(EventState.Idle)

  fun init(uuid: UUID) {
    if (root.isNotEmpty()) return
    root = uuid.toString()

    viewModelScope.launch {
      val profile = withProfile { queryByUUID(uuid) }
      if (profile == null) {
        eventState.value = EventState.Finish
        return@launch
      }
      uiState.update { it.copy(configurationEditable = profile.type == Profile.Type.Url) }
      fetch()
    }
  }

  fun consumeEvent() {
    eventState.value = EventState.Idle
  }

  override fun onStart(owner: LifecycleOwner) {
    if (root.isNotEmpty()) {
      fetch()
    }
  }

  fun onBack() {
    if (stack.isEmpty()) {
      eventState.value = EventState.Finish
    } else {
      stack.removeLast()
      fetch()
    }
  }

  fun onOpen(file: File) {
    if (file.isDirectory) {
      stack.addLast(file.id)
      fetch()
    } else {
      val uri = client.buildDocumentUri(file.id)
      eventState.value = EventState.OpenFile(uri)
    }
  }

  fun onDelete(file: File) {
    viewModelScope.launch {
      try {
        client.deleteDocument(file.id)
      } catch (e: Exception) {
        Log.e("Delete file failed: ${e.message}", e)
        eventState.value = EventState.ShowMessage(e.message ?: "Unknown error")
      }
      fetch()
    }
  }

  fun onRename(file: File, newName: String) {
    viewModelScope.launch {
      try {
        client.renameDocument(file.id, newName)
      } catch (e: Exception) {
        Log.e("Rename file failed: ${e.message}", e)
        eventState.value = EventState.ShowMessage(e.message ?: "Unknown error")
      }
      fetch()
    }
  }

  fun onRequestImport(file: File?) {
    eventState.value = EventState.RequestImport(file)
  }

  fun onImportResult(uri: Uri?, targetFile: File?) {
    if (uri == null) return
    val parentId = if (stack.isEmpty()) root else stack.last()
    viewModelScope.launch {
      try {
        if (targetFile == null) {
          client.importDocument(parentId, uri, uri.fileName ?: "File")
        } else {
          client.copyDocument(targetFile.id, uri)
        }
      } catch (e: Exception) {
        Log.e("Import file failed: ${e.message}", e)
        eventState.value = EventState.ShowMessage(e.message ?: "Unknown error")
      }
      fetch()
    }
  }

  fun onRequestExport(file: File) {
    eventState.value = EventState.RequestExport(file)
  }

  fun onExportResult(uri: Uri?, sourceFile: File?) {
    if (uri == null || sourceFile == null) return
    viewModelScope.launch {
      try {
        client.copyDocument(uri, sourceFile.id)
      } catch (e: Exception) {
        Log.e("Export file failed: ${e.message}", e)
        eventState.value = EventState.ShowMessage(e.message ?: "Unknown error")
      }
      fetch()
    }
  }

  private fun fetch() {
    fetchJob?.cancel()
    val documentId = stack.lastOrNull() ?: root
    if (root.isEmpty()) return
    val inBaseDir = stack.isEmpty()
    fetchJob = viewModelScope.launch {
      try {
        val files =
          if (inBaseDir) {
            val list = client.list(documentId)
            val config = list.firstOrNull { it.id.endsWith("config.yaml") }
            if (config == null || config.size > 0) list else listOf(config)
          } else {
            client.list(documentId)
          }

        uiState.update { it.copy(files = files, currentInBaseDir = inBaseDir) }
      } catch (e: Exception) {
        Log.e("List files failed: ${e.message}", e)
        eventState.value = EventState.ShowMessage(e.message ?: "Unknown error")
      }
    }
  }

  data class UiState(
    val files: List<File> = emptyList(),
    val currentInBaseDir: Boolean = true,
    val configurationEditable: Boolean = false,
  )

  sealed interface EventState {
    data object Idle : EventState

    data object Finish : EventState

    data class OpenFile(val uri: Uri) : EventState

    data class RequestImport(val targetFile: File?) : EventState

    data class RequestExport(val sourceFile: File) : EventState

    data class ShowMessage(val message: String) : EventState
  }
}
