package com.github.kr328.clash.profile.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.util.withProfile
import com.github.kr328.clash.profile.R
import com.github.kr328.clash.service.model.Profile
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class ProfilesViewModel(app: Application) :
  AndroidViewModel(app), DefaultLifecycleObserver {
  private var broadcastEventsJob: Job? = null
  private var elapsedJob: Job? = null
  private var fetchJob: Job? = null

  val uiState: StateFlow<UiState>
    field = MutableStateFlow(UiState())

  val eventState: StateFlow<EventState>
    field = MutableStateFlow<EventState>(EventState.Idle)

  override fun onStart(owner: LifecycleOwner) {
    broadcastEventsJob?.cancel()
    broadcastEventsJob = viewModelScope.launch {
      Remote.broadcasts.event.collect { event ->
        when (event) {
          ServiceRecreated,
          Started,
          ProfileChanged,
          ProfileLoaded -> fetch()
          is Stopped -> Unit
          is ProfileUpdateCompleted -> {
            event.uuid?.let { uuid -> showProfileUpdateCompleted(uuid) }
          }
          is ProfileUpdateFailed -> {
            event.uuid?.let { uuid -> showProfileUpdateFailed(uuid, event.reason) }
          }
        }
      }
    }

    startElapsedTicker()
    fetch()
  }

  override fun onStop(owner: LifecycleOwner) {
    broadcastEventsJob?.cancel()
    broadcastEventsJob = null
    elapsedJob?.cancel()
    elapsedJob = null
  }

  override fun onCleared() {
    broadcastEventsJob?.cancel()
    elapsedJob?.cancel()
    fetchJob?.cancel()
  }

  fun consumeEvent() {
    eventState.value = EventState.Idle
  }

  fun onOpenCreate() {
    eventState.value = EventState.OpenCreate
  }

  fun onActivate(profile: Profile) {
    viewModelScope.launch {
      if (profile.imported) {
        withProfile { setActive(profile) }
      } else {
        eventState.value =
          EventState.ShowEditableMessage(
            application.getString(R.string.active_unsaved_tips),
            profile.uuid,
          )
      }
    }
  }

  fun onUpdateAll() {
    if (uiState.value.allUpdating) return

    viewModelScope.launch {
      uiState.update { it.copy(allUpdating = true) }
      try {
        withProfile {
          queryAll().forEach { profile ->
            if (profile.imported && profile.type != File) {
              update(profile.uuid)
            }
          }
        }
      } finally {
        uiState.update { it.copy(allUpdating = false) }
      }
    }
  }

  fun onUpdate(profile: Profile) {
    viewModelScope.launch { withProfile { update(profile.uuid) } }
  }

  fun onEdit(profile: Profile) {
    eventState.value = EventState.OpenEdit(profile.uuid)
  }

  fun onDuplicate(profile: Profile) {
    viewModelScope.launch {
      val uuid = withProfile { clone(profile.uuid) }
      eventState.value = EventState.OpenEdit(uuid)
    }
  }

  fun onDelete(profile: Profile) {
    viewModelScope.launch { withProfile { delete(profile.uuid) } }
  }

  private fun fetch() {
    fetchJob?.cancel()
    fetchJob = viewModelScope.launch {
      val profiles = withProfile { queryAll() }
      val hasUpdatableProfile =
        withContext(Dispatchers.Default) { profiles.any { it.imported && it.type != File } }

      uiState.update { it.copy(profiles = profiles, hasUpdatableProfile = hasUpdatableProfile) }
    }
  }

  private fun startElapsedTicker() {
    if (elapsedJob?.isActive == true) return

    elapsedJob = viewModelScope.launch {
      while (isActive) {
        delay(1.minutes)
        uiState.update { it.copy(currentTime = System.currentTimeMillis()) }
      }
    }
  }

  private suspend fun showProfileUpdateCompleted(uuid: Uuid) {
    val name = withProfile { queryByUUID(uuid)?.name }
    eventState.value =
      EventState.ShowMessage(application.getString(R.string.toast_profile_updated_complete, name))
  }

  private suspend fun showProfileUpdateFailed(uuid: Uuid, reason: String?) {
    val name = withProfile { queryByUUID(uuid)?.name }
    val displayReason =
      reason?.takeUnless { it.isBlank() } ?: application.getString(CommonR.string.unknown)
    eventState.value =
      EventState.ShowEditableMessage(
        application.getString(R.string.toast_profile_updated_failed, name, displayReason),
        uuid,
      )
  }

  data class UiState(
    val profiles: List<Profile> = emptyList(),
    val allUpdating: Boolean = false,
    val hasUpdatableProfile: Boolean = false,
    val currentTime: Long = System.currentTimeMillis(),
  )

  sealed interface EventState {
    data object Idle : EventState

    data object OpenCreate : EventState

    data class OpenEdit(val uuid: Uuid) : EventState

    data class ShowMessage(val message: String) : EventState

    data class ShowEditableMessage(val message: String, val uuid: Uuid) : EventState
  }
}
