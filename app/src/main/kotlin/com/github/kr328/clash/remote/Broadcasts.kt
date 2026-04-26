package com.github.kr328.clash.remote

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.github.kr328.clash.common.compat.registerReceiverCompat
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.log.Log
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Broadcasts(private val context: Application) {
  sealed interface Event {
    val id: Long

    data object NotStart : Event {
      override val id: Long = 0L
    }

    data class ServiceRecreated(override val id: Long) : Event

    data class Started(override val id: Long) : Event

    data class Stopped(override val id: Long, val cause: String?) : Event

    data class ProfileChanged(override val id: Long) : Event

    data class ProfileUpdateCompleted(override val id: Long, val uuid: UUID?) : Event

    data class ProfileUpdateFailed(override val id: Long, val uuid: UUID?, val reason: String?) :
      Event

    data class ProfileLoaded(override val id: Long) : Event
  }

  val clashRunningFlow: StateFlow<Boolean>
    field = MutableStateFlow(false)

  val event: StateFlow<Event>
    field = MutableStateFlow<Event>(Event.NotStart)

  var clashRunning: Boolean
    get() = clashRunningFlow.value
    private set(value) {
      clashRunningFlow.value = value
    }

  private var registered = false
  private var nextEventId = 0L
  private val broadcastReceiver =
    object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.`package` != context?.packageName) return

        when (intent?.action) {
          Intents.ACTION_SERVICE_RECREATED -> {
            clashRunning = false
            emitEvent { id -> Event.ServiceRecreated(id) }
          }
          Intents.ACTION_CLASH_STARTED -> {
            clashRunning = true
            emitEvent { id -> Event.Started(id) }
          }
          Intents.ACTION_CLASH_STOPPED -> {
            clashRunning = false
            emitEvent { id -> Event.Stopped(id, intent.getStringExtra(Intents.EXTRA_STOP_REASON)) }
          }
          Intents.ACTION_PROFILE_CHANGED -> emitEvent { id -> Event.ProfileChanged(id) }
          Intents.ACTION_PROFILE_UPDATE_COMPLETED ->
            emitEvent { id ->
              Event.ProfileUpdateCompleted(
                id,
                UUID.fromString(intent.getStringExtra(Intents.EXTRA_UUID)),
              )
            }
          Intents.ACTION_PROFILE_UPDATE_FAILED ->
            emitEvent { id ->
              Event.ProfileUpdateFailed(
                id,
                UUID.fromString(intent.getStringExtra(Intents.EXTRA_UUID)),
                intent.getStringExtra(Intents.EXTRA_FAIL_REASON),
              )
            }
          Intents.ACTION_PROFILE_LOADED -> emitEvent { id -> Event.ProfileLoaded(id) }
        }
      }
    }

  private fun emitEvent(block: (Long) -> Event) {
    nextEventId += 1
    event.value = block(nextEventId)
  }

  fun register() {
    if (registered) return

    try {
      context.registerReceiverCompat(
        broadcastReceiver,
        IntentFilter().apply {
          addAction(Intents.ACTION_SERVICE_RECREATED)
          addAction(Intents.ACTION_CLASH_STARTED)
          addAction(Intents.ACTION_CLASH_STOPPED)
          addAction(Intents.ACTION_PROFILE_CHANGED)
          addAction(Intents.ACTION_PROFILE_UPDATE_COMPLETED)
          addAction(Intents.ACTION_PROFILE_UPDATE_FAILED)
          addAction(Intents.ACTION_PROFILE_LOADED)
        },
      )
      registered = true

      clashRunning = StatusClient(context).currentProfile() != null
    } catch (e: Exception) {
      Log.w("Register global receiver: $e", e)
    }
  }

  fun unregister() {
    if (!registered) return

    try {
      context.unregisterReceiver(broadcastReceiver)
      registered = false

      clashRunning = false
    } catch (e: Exception) {
      Log.w("Unregister global receiver: $e", e)
    }
  }
}
