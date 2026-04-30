package com.github.kr328.clash.remote

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.github.kr328.clash.common.compat.registerReceiverCompat
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.common.util.getSerializableCompat
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class Broadcasts(private val context: Application) {
  sealed interface Event {
    data object ServiceRecreated : Event

    data object Started : Event

    data class Stopped(val cause: String?) : Event

    data object ProfileChanged : Event

    data class ProfileUpdateCompleted(val uuid: Uuid?) : Event

    data class ProfileUpdateFailed(val uuid: Uuid?, val reason: String?) : Event

    data object ProfileLoaded : Event
  }

  val clashRunningFlow: StateFlow<Boolean>
    field = MutableStateFlow(false)

  val event: SharedFlow<Event>
    field = MutableSharedFlow(extraBufferCapacity = 64)

  var clashRunning: Boolean
    get() = clashRunningFlow.value
    private set(value) {
      clashRunningFlow.value = value
    }

  private var registered = false
  private val broadcastReceiver =
    object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.`package` != context?.packageName) return

        when (intent?.action) {
          Intents.ACTION_SERVICE_RECREATED -> {
            clashRunning = false
            event.tryEmit(Event.ServiceRecreated)
          }
          Intents.ACTION_CLASH_STARTED -> {
            clashRunning = true
            event.tryEmit(Event.Started)
          }
          Intents.ACTION_CLASH_STOPPED -> {
            clashRunning = false
            event.tryEmit(Event.Stopped(intent.getStringExtra(Intents.EXTRA_STOP_REASON)))
          }
          Intents.ACTION_PROFILE_CHANGED -> event.tryEmit(Event.ProfileChanged)
          Intents.ACTION_PROFILE_UPDATE_COMPLETED ->
            event.tryEmit(
              Event.ProfileUpdateCompleted(intent.getSerializableCompat(Intents.EXTRA_UUID))
            )
          Intents.ACTION_PROFILE_UPDATE_FAILED ->
            event.tryEmit(
              Event.ProfileUpdateFailed(
                intent.getSerializableCompat(Intents.EXTRA_UUID),
                intent.getStringExtra(Intents.EXTRA_FAIL_REASON),
              )
            )
          Intents.ACTION_PROFILE_LOADED -> event.tryEmit(Event.ProfileLoaded)
        }
      }
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
