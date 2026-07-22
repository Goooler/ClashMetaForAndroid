package com.github.kr328.clash.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.github.kr328.clash.common.util.Log
import com.github.kr328.clash.service.clash.clashRuntime
import com.github.kr328.clash.service.clash.module.AppListCacheModule
import com.github.kr328.clash.service.clash.module.CloseModule
import com.github.kr328.clash.service.clash.module.ConfigurationModule
import com.github.kr328.clash.service.clash.module.DynamicNotificationModule
import com.github.kr328.clash.service.clash.module.NetworkObserveModule
import com.github.kr328.clash.service.clash.module.StaticNotificationModule
import com.github.kr328.clash.service.clash.module.SuspendModule
import com.github.kr328.clash.service.clash.module.TimeZoneModule
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.service.util.cancelAndJoinBlocking
import com.github.kr328.clash.service.util.sendClashStarted
import com.github.kr328.clash.service.util.sendClashStopped
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext

class ClashService : BaseService() {
  private val self: ClashService
    get() = this

  private var reason: String? = null

  private val runtime = clashRuntime {
    val store = ServiceStore(self)

    val close = install(CloseModule(self))
    val config = install(ConfigurationModule(self))
    val network = install(NetworkObserveModule(self))

    if (store.dynamicNotification) install(DynamicNotificationModule(self))
    else install(StaticNotificationModule(self))

    install(AppListCacheModule(self))
    install(TimeZoneModule(self))
    install(SuspendModule(self))

    try {
      while (isActive) {
        val quit = select {
          close.onEvent { true }
          config.onEvent {
            reason = it.message

            true
          }
          network.onEvent { false }
        }

        if (quit) break
      }
    } catch (e: Exception) {
      Log.e("Create clash runtime: ${e.message}", e)

      reason = e.message
    } finally {
      withContext(NonCancellable) { stopSelf() }
    }
  }

  override fun onCreate() {
    super.onCreate()

    if (StatusProvider.serviceRunning) return stopSelf()

    StatusProvider.currentProfile = null
    StatusProvider.serviceRunning = true

    StaticNotificationModule.createNotificationChannel(this)
    StaticNotificationModule.notifyLoadingNotification(this)

    runtime.launch()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    sendClashStarted()

    return START_STICKY
  }

  override fun onBind(intent: Intent?): IBinder {
    return Binder()
  }

  override fun onDestroy() {
    StatusProvider.currentProfile = null
    StatusProvider.serviceRunning = false

    sendClashStopped(reason)

    cancelAndJoinBlocking()

    Log.i("ClashService destroyed: ${reason ?: "successfully"}")

    super.onDestroy()
  }

  override fun onTrimMemory(level: Int) {
    super.onTrimMemory(level)

    runtime.requestGc()
  }
}
