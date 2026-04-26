package com.github.kr328.clash.ui

import android.app.ActivityManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import com.github.kr328.clash.core.bridge.ClashException
import com.github.kr328.clash.model.DarkMode
import com.github.kr328.clash.remote.Broadcasts
import com.github.kr328.clash.remote.Remote
import com.github.kr328.clash.store.UiStore
import com.github.kr328.clash.util.ActivityResultLifecycleOwner
import com.github.kr328.clash.util.ApplicationObserver
import com.github.kr328.clash.util.showExceptionSnackbar
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

abstract class BaseActivity : ComponentActivity() {
  protected val uiStore by lazy { UiStore(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Apply excludeFromRecents setting to all app tasks.
    checkNotNull(getSystemService<ActivityManager>()).appTasks.forEach { task ->
      task.setExcludeFromRecents(uiStore.hideFromRecents)
    }
  }
}

abstract class DesignActivity<D : Design<*>> : BaseActivity(), CoroutineScope by MainScope() {
  protected val events = Channel<Event>(Channel.UNLIMITED)
  protected val clashRunning: Boolean
    get() = Remote.broadcasts.clashRunning

  protected var activityStarted: Boolean = false
  protected var design: D? = null

  private val nextRequestKey = AtomicInteger(0)
  private var broadcastEventsJob: Job? = null
  private var defer: suspend () -> Unit = {}
  private var deferRunning = false
  private var dayNight: DayNight = DayNight.Day

  protected open suspend fun main() = Unit

  fun defer(operation: suspend () -> Unit) {
    this.defer = operation
  }

  suspend fun setContentDesign(design: D) =
    withContext(Dispatchers.Main) {
      this@DesignActivity.design = design
      setContent(content = design::Content)
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    launch { main() }
  }

  override fun onStart() {
    super.onStart()
    broadcastEventsJob = lifecycleScope.launch {
      Remote.broadcasts.event.collect { event ->
        when (event) {
          Broadcasts.Event.ServiceRecreated -> events.trySend(Event.ServiceRecreated)
          Broadcasts.Event.Started -> events.trySend(Event.ClashStart)
          is Broadcasts.Event.Stopped -> {
            events.trySend(Event.ClashStop)

            if (event.cause != null && activityStarted) {
              launch { design?.showExceptionSnackbar(ClashException(event.cause)) }
            }
          }
          Broadcasts.Event.ProfileChanged -> events.trySend(Event.ProfileChanged)
          is Broadcasts.Event.ProfileUpdateCompleted ->
            events.trySend(Event.ProfileUpdateCompleted(event.uuid))
          is Broadcasts.Event.ProfileUpdateFailed ->
            events.trySend(Event.ProfileUpdateFailed(event.uuid, event.reason))
          Broadcasts.Event.ProfileLoaded -> events.trySend(Event.ProfileLoaded)
        }
      }
    }
    activityStarted = true
    events.trySend(Event.ActivityStart)
  }

  override fun onStop() {
    super.onStop()
    broadcastEventsJob?.cancel()
    broadcastEventsJob = null
    activityStarted = false
    events.trySend(Event.ActivityStop)
  }

  override fun onDestroy() {
    cancel()
    super.onDestroy()
  }

  override fun finish() {
    if (deferRunning) return
    deferRunning = true

    launch {
      try {
        defer()
      } finally {
        withContext(NonCancellable) { super.finish() }
      }
    }
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)

    if (queryDayNight(newConfig) != dayNight) {
      ApplicationObserver.createdActivities.forEach { it.recreate() }
    }
  }

  suspend fun <I, O> ActivityResultContract<I, O>.startForResult(input: I): O =
    withContext(Dispatchers.Main) {
      val requestKey = nextRequestKey.getAndIncrement().toString()

      ActivityResultLifecycleOwner().use { owner, start ->
        suspendCancellableCoroutine { c ->
          activityResultRegistry
            .register(
              key = requestKey,
              lifecycleOwner = owner,
              contract = this@startForResult,
              callback = c::resume,
            )
            .apply { start() }
            .launch(input)
        }
      }
    }

  private fun queryDayNight(config: Configuration = resources.configuration): DayNight {
    return when (uiStore.darkMode) {
      DarkMode.Auto ->
        if (config.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES)
          DayNight.Night
        else DayNight.Day
      DarkMode.ForceLight -> DayNight.Day
      DarkMode.ForceDark -> DayNight.Night
    }
  }

  sealed interface Event {
    data object ServiceRecreated : Event

    data object ActivityStart : Event

    data object ActivityStop : Event

    data object ClashStop : Event

    data object ClashStart : Event

    data object ProfileLoaded : Event

    data object ProfileChanged : Event

    data class ProfileUpdateCompleted(val uuid: UUID?) : Event

    data class ProfileUpdateFailed(val uuid: UUID?, val reason: String?) : Event
  }
}
