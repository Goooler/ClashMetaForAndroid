package com.github.kr328.clash.profile.vm

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.Res as CommonRes
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.common.unknown
import com.github.kr328.clash.core.model.FetchStatus
import com.github.kr328.clash.glue.util.withProfile
import com.github.kr328.clash.profile.Res
import com.github.kr328.clash.profile.empty_name
import com.github.kr328.clash.profile.format_fetching_configuration
import com.github.kr328.clash.profile.format_fetching_provider
import com.github.kr328.clash.profile.initializing
import com.github.kr328.clash.profile.invalid_url
import com.github.kr328.clash.profile.verifying
import com.github.kr328.clash.service.model.Profile
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString

internal class PropertiesViewModel(
  private val application: Application,
  private val scope: CoroutineScope,
) : ViewModel(), DefaultLifecycleObserver {
  private var rootUuid: Uuid? = null
  private var canceled = false

  val uiState: StateFlow<UiState>
    field = MutableStateFlow(UiState())

  val eventState: SharedFlow<EventState>
    field = MutableSharedFlow(extraBufferCapacity = 64)

  fun init(uuid: Uuid) {
    if (rootUuid != null) return
    rootUuid = uuid

    viewModelScope.launch {
      val profile = withProfile { queryByUUID(uuid) }
      if (profile == null) {
        eventState.tryEmit(EventState.Finish(false))
        return@launch
      }
      uiState.update { it.copy(profile = profile, originalProfile = profile.copy()) }
    }
  }

  override fun onStop(owner: LifecycleOwner) {
    if (!canceled && uiState.value.hasUnsavedChanges) {
      val profile = uiState.value.profile ?: return
      viewModelScope.launch {
        runCatching {
          withProfile {
            patch(
              profile.uuid,
              profile.name,
              profile.source,
              profile.interval,
              profile.ageSecretKey,
            )
          }
        }
          .onFailure { e -> Log.e("Auto save profile failed: ${e.message}", e) }
          .onSuccess {
            uiState.update { state ->
              state.copy(originalProfile = profile.copy(), hasUnsavedChanges = false)
            }
          }
      }
    }
  }

  override fun onCleared() {
    rootUuid?.let { uuid ->
      scope.launch {
        try {
          withProfile { release(uuid) }
        } catch (e: Exception) {
          Log.e("Release profile failed: ${e.message}", e)
        }
      }
    }
  }

  fun onNameChanged(name: String) {
    uiState.update { current ->
      val profile = current.profile?.copy(name = name) ?: return@update current
      current.copy(
        profile = profile,
        hasUnsavedChanges = hasUnsavedChanges(profile, current.originalProfile),
      )
    }
  }

  fun onUrlChanged(url: String) {
    uiState.update { current ->
      val profile = current.profile?.copy(source = url) ?: return@update current
      current.copy(
        profile = profile,
        hasUnsavedChanges = hasUnsavedChanges(profile, current.originalProfile),
      )
    }
  }

  fun onIntervalChanged(interval: Long) {
    uiState.update { current ->
      val profile = current.profile?.copy(interval = interval) ?: return@update current
      current.copy(
        profile = profile,
        hasUnsavedChanges = hasUnsavedChanges(profile, current.originalProfile),
      )
    }
  }

  fun onAgeSecretKeyChanged(key: String?) {
    uiState.update { current ->
      val profile = current.profile?.copy(ageSecretKey = key) ?: return@update current
      current.copy(
        profile = profile,
        hasUnsavedChanges = hasUnsavedChanges(profile, current.originalProfile),
      )
    }
  }

  fun onBrowseFiles() {
    val uuid = rootUuid ?: return
    eventState.tryEmit(EventState.BrowseFiles(uuid))
  }

  fun onRequestClose() {
    canceled = true
    eventState.tryEmit(EventState.Finish(false))
  }

  fun onCommit() {
    val profile = uiState.value.profile ?: return

    viewModelScope.launch {
      if (profile.name.isBlank()) {
        eventState.tryEmit(EventState.ShowMessage(getString(Res.string.empty_name)))
        return@launch
      }

      if (profile.type != File && profile.source.isBlank()) {
        eventState.tryEmit(EventState.ShowMessage(getString(Res.string.invalid_url)))
        return@launch
      }

      try {
        withProcessing { updateStatus ->
          withProfile {
            patch(
              profile.uuid,
              profile.name,
              profile.source,
              profile.interval,
              profile.ageSecretKey,
            )
            coroutineScope { commit(profile.uuid) { launch { updateStatus(it) } } }
          }
        }
        canceled = true
        eventState.tryEmit(EventState.Finish(true))
      } catch (e: Exception) {
        Log.e("Commit profile failed: ${e.message}", e)
        eventState.tryEmit(EventState.ShowMessage(e.message ?: getString(CommonRes.string.unknown)))
      }
    }
  }

  private suspend fun withProcessing(executeTask: suspend (suspend (FetchStatus) -> Unit) -> Unit) {
    try {
      withContext(Dispatchers.Main) {
        uiState.update {
          it.copy(
            processing = true,
            progress =
              ProgressState(
                visible = true,
                isIndeterminate = true,
                text = getString(Res.string.initializing),
                progress = 0,
                max = 0,
              ),
          )
        }
      }

      executeTask { status -> withContext(Dispatchers.Main) { applyProgressStatus(status) } }
    } finally {
      withContext(Dispatchers.Main) {
        uiState.update {
          it.copy(processing = false, progress = it.progress.copy(visible = false, text = null))
        }
      }
    }
  }

  private suspend fun applyProgressStatus(status: FetchStatus) {
    uiState.update { current ->
      val newProgress =
        when (status.action) {
          FetchConfiguration -> {
            current.progress.copy(
              text =
                getString(
                  Res.string.format_fetching_configuration,
                  status.args.getOrNull(0).orEmpty(),
                ),
              isIndeterminate = true,
            )
          }
          FetchProviders -> {
            current.progress.copy(
              text =
                getString(
                  Res.string.format_fetching_provider,
                  status.args.getOrNull(0).orEmpty(),
                ),
              isIndeterminate = false,
              max = status.max,
              progress = status.progress,
            )
          }
          Verifying -> {
            current.progress.copy(
              text = getString(Res.string.verifying),
              isIndeterminate = false,
              max = status.max,
              progress = status.progress,
            )
          }
          SubscriptionInfo -> current.progress
        }
      current.copy(progress = newProgress)
    }
  }

  private fun hasUnsavedChanges(profile: Profile, original: Profile?): Boolean {
    if (original == null) return false
    return profile.name != original.name ||
      profile.source != original.source ||
      profile.interval != original.interval ||
      profile.ageSecretKey != original.ageSecretKey
  }

  data class UiState(
    val profile: Profile? = null,
    val originalProfile: Profile? = null,
    val processing: Boolean = false,
    val progress: ProgressState = ProgressState(),
    val hasUnsavedChanges: Boolean = false,
  )

  data class ProgressState(
    val visible: Boolean = false,
    val isIndeterminate: Boolean = false,
    val text: String? = null,
    val progress: Int = 0,
    val max: Int = 0,
  )

  sealed interface EventState {
    data class Finish(val success: Boolean) : EventState

    data class BrowseFiles(val uuid: Uuid) : EventState

    data class ShowMessage(val message: String) : EventState
  }
}
