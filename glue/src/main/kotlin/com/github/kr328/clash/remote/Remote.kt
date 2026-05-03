package com.github.kr328.clash.remote

import android.content.Context
import android.content.Intent
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.Global.application
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.store.AppStore
import com.github.kr328.clash.util.ApplicationObserver
import com.github.kr328.clash.util.mainIntent
import com.github.kr328.clash.util.verifyApk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Remote {
  val broadcasts: Broadcasts = Broadcasts(application)
  val service: Service =
    Service(application) {
      ApplicationObserver.createdActivities.forEach { it.finish() }

      val intent =
        mainIntent(context = application, action = Intents.ACTION_APP_CRASHED)
          .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

      application.startActivity(intent)
    }

  fun launch() {
    ApplicationObserver.attach(application)

    ApplicationObserver.onVisibleChanged {
      if (it) {
        Log.d("App becomes visible")
        service.bind()
        broadcasts.register()
      } else {
        Log.d("App becomes invisible")
        service.unbind()
        broadcasts.unregister()
      }
    }

    Global.launch(Dispatchers.IO) { verifyApp() }
  }

  private fun verifyApp() {
    val context = application
    val store = AppStore(context)
    val updatedAt = getLastUpdated(context)

    if (store.updatedAt != updatedAt) {
      if (!context.verifyApk()) {
        ApplicationObserver.createdActivities.forEach { it.finish() }

        val intent =
          mainIntent(context = application, action = Intents.ACTION_APK_BROKEN)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

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
