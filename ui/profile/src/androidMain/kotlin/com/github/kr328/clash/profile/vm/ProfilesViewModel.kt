package com.github.kr328.clash.profile.vm

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.Res as CommonRes
import com.github.kr328.clash.common.unknown
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.util.withProfile
import com.github.kr328.clash.profile.Res
import com.github.kr328.clash.profile.active_unsaved_tips
import com.github.kr328.clash.profile.toast_profile_updated_complete
import com.github.kr328.clash.profile.toast_profile_updated_failed
import com.github.kr328.clash.service.model.Profile
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString

internal class ProfilesViewModel(private val application: Application) :
  ViewModel(), DefaultLifecycleObserver {
  private var broadcastEventsJob: Job? = null
  private var elapsedJob: Job? = null
  private var fetchJob: Job? = null

  val uiState: StateFlow<UiState>
    field = MutableStateFlow(UiState())

  val event: SharedFlow<Event>
    field = MutableSharedFlow(extraBufferCapacity = 64)

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

  fun onOpenCreate() {
    event.tryEmit(Event.OpenCreate)
  }

  fun onActivate(profile: Profile) {
    viewModelScope.launch {
      if (profile.imported) {
        withProfile { setActive(profile) }
      } else {
        event.tryEmit(
          Event.ShowEditableMessage(
            getString(Res.string.active_unsaved_tips),
            profile.uuid,
          )
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
    event.tryEmit(Event.OpenEdit(profile.uuid))
  }

  fun onDuplicate(profile: Profile) {
    viewModelScope.launch {
      val uuid = withProfile { clone(profile.uuid) }
      event.tryEmit(Event.OpenEdit(uuid))
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
    val name = withProfile { queryByUUID(uuid)?.name.orEmpty() }
    event.tryEmit(Event.ShowMessage(getString(Res.string.toast_profile_updated_complete, name)))
  }

  private suspend fun showProfileUpdateFailed(uuid: Uuid, reason: String?) {
    val name = withProfile { queryByUUID(uuid)?.name.orEmpty() }
    val displayReason = reason?.takeUnless { it.isBlank() } ?: getString(CommonRes.string.unknown)
    event.tryEmit(
      Event.ShowEditableMessage(
        getString(Res.string.toast_profile_updated_failed, name, displayReason),
        uuid,
      )
    )
  }

  data class UiState(
    val profiles: List<Profile> = emptyList(),
    val allUpdating: Boolean = false,
    val hasUpdatableProfile: Boolean = false,
    val currentTime: Long = System.currentTimeMillis(),
  )

  sealed interface Event {
    data object OpenCreate : Event

    data class OpenEdit(val uuid: Uuid) : Event

    data class ShowMessage(val message: String) : Event

    data class ShowEditableMessage(val message: String, val uuid: Uuid) : Event
  }
}
