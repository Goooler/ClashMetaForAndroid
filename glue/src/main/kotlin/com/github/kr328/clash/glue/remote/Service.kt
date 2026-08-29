package com.github.kr328.clash.glue.remote

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import co.touchlab.kermit.Logger
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.glue.util.unbindServiceSilent
import com.github.kr328.clash.service.RemoteService
import com.github.kr328.clash.service.remote.IRemoteService
import com.github.kr328.clash.service.remote.unwrap
import kotlin.time.Duration.Companion.seconds

class Service(private val context: Application, val crashed: () -> Unit) {
  val remote = Resource<IRemoteService>()

  private val connection =
    object : ServiceConnection {
      private var lastCrashed: Long = -1

      override fun onServiceConnected(name: ComponentName?, service: IBinder) {
        remote.set(service.unwrap(IRemoteService::class))
      }

      override fun onServiceDisconnected(name: ComponentName?) {
        remote.set(null)

        if (System.currentTimeMillis() - lastCrashed < TOGGLE_CRASHED_INTERVAL) {
          unbind()

          crashed()
        }

        lastCrashed = System.currentTimeMillis()
        Logger.w("RemoteService killed or crashed")
      }
    }

  fun bind() {
    try {
      context.bindService(RemoteService::class.intent, connection, Context.BIND_AUTO_CREATE)
    } catch (e: Exception) {
      Logger.e("Bind remote service failed: ${e.message}", e)
      unbind()

      crashed()
    }
  }

  fun unbind() {
    context.unbindServiceSilent(connection)

    remote.set(null)
  }

  companion object {
    private val TOGGLE_CRASHED_INTERVAL = 10.seconds.inWholeMilliseconds
  }
}
