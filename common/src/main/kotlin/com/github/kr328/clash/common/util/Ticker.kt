package com.github.kr328.clash.common.util

import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

fun CoroutineScope.ticker(duration: Duration): Channel<Long> {
  val channel = Channel<Long>(Channel.RENDEZVOUS)

  launch {
    try {
      while (isActive) {
        channel.send(System.currentTimeMillis())

        delay(duration)
      }
    } catch (ignored: Exception) {}
  }

  return channel
}
