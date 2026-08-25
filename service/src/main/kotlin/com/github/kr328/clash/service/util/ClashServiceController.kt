package com.github.kr328.clash.service.util

import android.app.Service
import android.app.Service.STOP_FOREGROUND_REMOVE
import com.github.kr328.clash.service.StatusProvider
import com.github.kr328.clash.service.clash.module.StaticNotificationModule

class ClashServiceController(
  private val service: Service,
  private val launchRuntime: () -> Unit,
) {
  private var lastStartId = 0

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

  fun onFinished(reason: String?) {
    StatusProvider.currentProfile = null
    StatusProvider.serviceRunning = false

    service.sendClashStopped(reason)

    service.stopForeground(STOP_FOREGROUND_REMOVE)

    service.stopSelf(lastStartId)
  }

  fun onDestroy(reason: String?) {
    if (StatusProvider.serviceRunning) {
      StatusProvider.currentProfile = null
      StatusProvider.serviceRunning = false

      service.sendClashStopped(reason)
    }
  }
}
