package com.github.kr328.clash.common.util

import co.touchlab.kermit.Logger
import kotlin.time.Clock
import kotlin.time.Duration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

fun CoroutineScope.ticker(duration: Duration, clock: Clock = Clock.System): Channel<Long> {
  val channel = Channel<Long>(Channel.RENDEZVOUS)

  launch {
    try {
      while (isActive) {
        channel.send(clock.now().toEpochMilliseconds())

        delay(duration)
      }
    } catch (e: Exception) {
      if (e !is CancellationException) {
        Logger.e("Ticker stopped unexpectedly: ${e.message}", e)
      }
      throw e
    }
  }

  return channel
}
