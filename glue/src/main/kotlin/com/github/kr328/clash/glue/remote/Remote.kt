package com.github.kr328.clash.glue.remote

import android.app.Application
import android.content.Context
import android.content.Intent
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.common.util.mainIntent
import com.github.kr328.clash.glue.store.AppStore
import com.github.kr328.clash.glue.util.ApplicationObserver
import com.github.kr328.clash.glue.util.verifyApk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object Remote : KoinComponent {
  private val application: Application by inject(mode = NONE)
  private val scope: CoroutineScope by inject(mode = NONE)

  val broadcasts: Broadcasts by lazy { Broadcasts(application) }
  val service: Service by lazy {
    Service(application) {
      ApplicationObserver.createdActivities.forEach { it.finish() }

      val intent = application.mainIntent {
        action = Intents.ACTION_APP_CRASHED
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      application.startActivity(intent)
    }
  }

  fun launch() {
    broadcasts.register()
    ApplicationObserver.attach(application)
    ApplicationObserver.onVisibleChanged {
      if (it) {
        Log.d("App becomes visible")
        service.bind()
      } else {
        Log.d("App becomes invisible")
        service.unbind()
      }
    }

    scope.launch { verifyApp() }
  }

  private fun verifyApp() {
    val context = application
    val store = AppStore(context)
    val updatedAt = getLastUpdated(context)

    if (store.updatedAt != updatedAt) {
      if (!context.verifyApk()) {
        ApplicationObserver.createdActivities.forEach { it.finish() }

        val intent = application.mainIntent {
          action = Intents.ACTION_APK_BROKEN
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return context.startActivity(intent)
      } else {
        store.updatedAt = updatedAt
      }
    }
  }

  private fun getLastUpdated(context: Context): Long {
    return context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
  }
}
