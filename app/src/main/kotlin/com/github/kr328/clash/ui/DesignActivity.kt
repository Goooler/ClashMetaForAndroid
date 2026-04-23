package com.github.kr328.clash.ui

import android.app.ActivityManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.getSystemService
import com.github.kr328.clash.core.bridge.ClashException
import com.github.kr328.clash.model.DarkMode
import com.github.kr328.clash.remote.Broadcasts
import com.github.kr328.clash.remote.Remote
import com.github.kr328.clash.store.UiStore
import com.github.kr328.clash.util.ActivityResultLifecycle
import com.github.kr328.clash.util.ApplicationObserver
import com.github.kr328.clash.util.showExceptionSnackbar
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

abstract class BaseActivity : ComponentActivity(), Broadcasts.Observer {
  private val nextRequestKey = AtomicInteger(0)
  protected var activityStarted: Boolean = false
  protected val uiStore by lazy { UiStore(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Apply excludeFromRecents setting to all app tasks.
    checkNotNull(getSystemService<ActivityManager>()).appTasks.forEach { task ->
      task.setExcludeFromRecents(uiStore.hideFromRecents)
    }
  }

  override fun onStart() {
    super.onStart()
    activityStarted = true
    Remote.broadcasts.addObserver(this)
  }

  override fun onStop() {
    super.onStop()
    activityStarted = false
    Remote.broadcasts.removeObserver(this)
  }

  suspend fun <I, O> startActivityForResult(contracts: ActivityResultContract<I, O>, input: I): O =
    withContext(Dispatchers.Main) {
      val requestKey = nextRequestKey.getAndIncrement().toString()

      ActivityResultLifecycle().use { lifecycle, start ->
        suspendCancellableCoroutine { c ->
          activityResultRegistry
            .register(requestKey, lifecycle, contracts) { c.resume(it) }
            .apply { start() }
            .launch(input)
        }
      }
    }
}

abstract class DesignActivity<D : Design<*>> : BaseActivity(), CoroutineScope by MainScope() {
  protected val events = Channel<Event>(Channel.UNLIMITED)
  protected val clashRunning: Boolean
    get() = Remote.broadcasts.clashRunning

  protected var design: D? = null

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
    events.trySend(Event.ActivityStart)
  }

  override fun onStop() {
    super.onStop()
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

  override fun onProfileChanged() {
    events.trySend(Event.ProfileChanged)
  }

  override fun onProfileUpdateCompleted(uuid: UUID?) {
    events.trySend(Event.ProfileUpdateCompleted)
  }

  override fun onProfileUpdateFailed(uuid: UUID?, reason: String?) {
    events.trySend(Event.ProfileUpdateFailed)
  }

  override fun onProfileLoaded() {
    events.trySend(Event.ProfileLoaded)
  }

  override fun onServiceRecreated() {
    events.trySend(Event.ServiceRecreated)
  }

  override fun onStarted() {
    events.trySend(Event.ClashStart)
  }

  override fun onStopped(cause: String?) {
    events.trySend(Event.ClashStop)

    if (cause != null && activityStarted) {
      launch { design?.showExceptionSnackbar(ClashException(cause)) }
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

    data object ProfileUpdateCompleted : Event

    data object ProfileUpdateFailed : Event
  }
}
