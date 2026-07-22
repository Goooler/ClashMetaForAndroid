package com.github.kr328.clash.home.vm

import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

internal interface Broadcasts {
  val clashRunning: StateFlow<Boolean>

  val profileLoaded: StateFlow<Boolean>

  val events: Flow<Event>
}

internal sealed interface Event {
  data object ServiceRecreated : Event

  data object Started : Event

  data class Stopped(val cause: String?) : Event

  data object ProfileChanged : Event

  data class ProfileUpdateCompleted(val uuid: Uuid?) : Event

  data class ProfileUpdateFailed(val uuid: Uuid?, val reason: String?) : Event

  data object ProfileLoaded : Event
}

internal expect val platformBroadcasts: Broadcasts
