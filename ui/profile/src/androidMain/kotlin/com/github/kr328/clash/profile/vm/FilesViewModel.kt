package com.github.kr328.clash.profile.vm

import android.app.Application
import android.net.Uri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.glue.model.ConfigFile
import com.github.kr328.clash.glue.remote.FilesClient
import com.github.kr328.clash.glue.util.fileName
import com.github.kr328.clash.glue.util.withProfile
import kotlin.uuid.Uuid
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class FilesViewModel(private val application: Application) :
  ViewModel(), DefaultLifecycleObserver {
  private val client = FilesClient(application)
  private val stack = ArrayDeque<String>()
  private var root: String = ""
  private var fetchJob: Job? = null

  val uiState: StateFlow<UiState>
    field = MutableStateFlow(UiState())

  val eventState: StateFlow<EventState>
    field = MutableStateFlow<EventState>(EventState.Idle)

  fun init(uuid: Uuid) {
    if (root.isNotEmpty()) return
    root = uuid.toString()

    viewModelScope.launch {
      val profile = withProfile { queryByUUID(uuid) }
      if (profile == null) {
        eventState.value = EventState.Finish
        return@launch
      }
      uiState.update { it.copy(configurationEditable = profile.type == Url) }
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

  fun onOpen(configFile: ConfigFile) {
    if (configFile.isDirectory) {
      stack.addLast(configFile.id)
      fetch()
    } else {
      val uri = client.buildDocumentUri(configFile.id)
      eventState.value = EventState.OpenFile(uri)
    }
  }

  fun onDelete(configFile: ConfigFile) {
    viewModelScope.launch {
      try {
        client.deleteDocument(configFile.id)
      } catch (e: Exception) {
        Log.e("Delete file failed: ${e.message}", e)
        eventState.value = EventState.ShowMessage(e.message ?: "Unknown error")
      }
      fetch()
    }
  }

  fun onRename(configFile: ConfigFile, newName: String) {
    viewModelScope.launch {
      try {
        client.renameDocument(configFile.id, newName)
      } catch (e: Exception) {
        Log.e("Rename file failed: ${e.message}", e)
        eventState.value = EventState.ShowMessage(e.message ?: "Unknown error")
      }
      fetch()
    }
  }

  fun onRequestImport(configFile: ConfigFile?) {
    eventState.value = EventState.RequestImport(configFile)
  }

  fun onImportResult(uri: Uri?, targetConfigFile: ConfigFile?) {
    if (uri == null) return
    val parentId = if (stack.isEmpty()) root else stack.last()
    viewModelScope.launch {
      try {
        if (targetConfigFile == null) {
          client.importDocument(parentId, uri, uri.fileName ?: "File")
        } else {
          client.copyDocument(targetConfigFile.id, uri)
        }
      } catch (e: Exception) {
        Log.e("Import file failed: ${e.message}", e)
        eventState.value = EventState.ShowMessage(e.message ?: "Unknown error")
      }
      fetch()
    }
  }

  fun onRequestExport(configFile: ConfigFile) {
    eventState.value = EventState.RequestExport(configFile)
  }

  fun onExportResult(uri: Uri?, sourceConfigFile: ConfigFile?) {
    if (uri == null || sourceConfigFile == null) return
    viewModelScope.launch {
      try {
        client.copyDocument(uri, sourceConfigFile.id)
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

        uiState.update { it.copy(configFiles = files, currentInBaseDir = inBaseDir) }
      } catch (e: Exception) {
        Log.e("List files failed: ${e.message}", e)
        eventState.value = EventState.ShowMessage(e.message ?: "Unknown error")
      }
    }
  }

  data class UiState(
    val configFiles: List<ConfigFile> = emptyList(),
    val currentInBaseDir: Boolean = true,
    val configurationEditable: Boolean = false,
  )

  sealed interface EventState {
    data object Idle : EventState

    data object Finish : EventState

    data class OpenFile(val uri: Uri) : EventState

    data class RequestImport(val targetConfigFile: ConfigFile?) : EventState

    data class RequestExport(val sourceConfigFile: ConfigFile) : EventState

    data class ShowMessage(val message: String) : EventState
  }
}
