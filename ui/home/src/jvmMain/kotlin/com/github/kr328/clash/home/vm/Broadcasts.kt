package com.github.kr328.clash.home.vm

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow

internal actual val platformBroadcasts: Broadcasts =
  object : Broadcasts {
    override val clashRunning = MutableStateFlow(false)
    override val profileLoaded = MutableStateFlow(false)
    override val events = emptyFlow<Event>()
  }
