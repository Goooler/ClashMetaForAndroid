package com.github.kr328.clash.service.util

import android.app.Service
import android.app.Service.STOP_FOREGROUND_REMOVE
import com.github.kr328.clash.service.StatusProvider
import com.github.kr328.clash.service.clash.module.StaticNotificationModule

/**
 * Controller to encapsulate lifecycle management, status coordination, and start/stop broadcast
 * handling for Clash background services (TunService and ClashService).
 */
class ClashServiceController(
  private val service: Service,
  private val launchRuntime: () -> Unit,
) {
  // Track the latest startId assigned by the system to avoid stopping a newly started session
  private var lastStartId = 0

  /**
   * Called during Service onStartCommand. If the service is not currently running, initialize
   * status, post loading notification, broadcast started state, and launch the runtime.
   */
  fun onStart(startId: Int) {
    lastStartId = startId

    if (!StatusProvider.serviceRunning) {
      StatusProvider.currentProfile = null
      StatusProvider.serviceRunning = true

      StaticNotificationModule.notifyLoadingNotification(service)

      service.sendClashStarted()

      launchRuntime()
    } else {
      service.sendClashStarted()
    }
  }

  /**
   * Called when runtime coroutine execution finishes (in the finally block). Immediately updates
   * state, broadcasts stopped state to unlock UI without waiting for AMS onDestroy, removes
   * foreground notification, and requests service shutdown for lastStartId.
   */
  fun onFinished(reason: String?) {
    StatusProvider.currentProfile = null
    StatusProvider.serviceRunning = false

    // Notify UI immediately that Clash is stopped
    service.sendClashStopped(reason)

    // Remove foreground notification immediately to accelerate AMS teardown
    service.stopForeground(STOP_FOREGROUND_REMOVE)

    // Only stop if no newer startId was issued during teardown
    service.stopSelf(lastStartId)
  }

  /**
   * Called during Service onDestroy as a fallback. Ensures stopped state is broadcast if the
   * service was killed abnormally (e.g. system low memory kill).
   */
  fun onDestroy(reason: String?) {
    if (StatusProvider.serviceRunning) {
      StatusProvider.currentProfile = null
      StatusProvider.serviceRunning = false

      service.sendClashStopped(reason)
    }
  }
}
