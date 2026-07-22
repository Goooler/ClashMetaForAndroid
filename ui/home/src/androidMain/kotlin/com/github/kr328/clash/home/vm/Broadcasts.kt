package com.github.kr328.clash.home.vm

import com.github.kr328.clash.glue.remote.Broadcasts as AndroidBroadcasts
import com.github.kr328.clash.glue.remote.Remote
import kotlinx.coroutines.flow.map

internal actual val platformBroadcasts: Broadcasts =
  object : Broadcasts {
    override val clashRunning = Remote.broadcasts.clashRunningFlow
    override val profileLoaded = Remote.broadcasts.profileLoadedFlow
    override val events = Remote.broadcasts.event.map(AndroidBroadcasts.Event::toSharedEvent)
  }

private fun AndroidBroadcasts.Event.toSharedEvent(): Event =
  when (this) {
    AndroidBroadcasts.Event.ServiceRecreated -> Event.ServiceRecreated
    AndroidBroadcasts.Event.Started -> Event.Started
    is AndroidBroadcasts.Event.Stopped -> Event.Stopped(cause)
    AndroidBroadcasts.Event.ProfileChanged -> Event.ProfileChanged
    is AndroidBroadcasts.Event.ProfileUpdateCompleted -> Event.ProfileUpdateCompleted(uuid)
    is AndroidBroadcasts.Event.ProfileUpdateFailed -> Event.ProfileUpdateFailed(uuid, reason)
    AndroidBroadcasts.Event.ProfileLoaded -> Event.ProfileLoaded
  }
