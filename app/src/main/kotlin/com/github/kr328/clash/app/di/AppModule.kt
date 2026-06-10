package com.github.kr328.clash.app.di

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import com.github.kr328.clash.app.BuildConfig
import com.github.kr328.clash.app.MainActivity
import com.github.kr328.clash.app.RestartReceiver
import com.github.kr328.clash.common.compat.queryIntentActivitiesCompat
import com.github.kr328.clash.common.di.AppInfoProvider
import com.github.kr328.clash.common.di.AppInfoProvider.Companion.appInfoProvider
import com.github.kr328.clash.glue.store.UiStore
import kotlin.reflect.KClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val appModule = module {
  single<AppInfoProvider> { AppInfoProviderImpl(application = get()) }
  single<CoroutineScope> { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
  single { UiStore(application = get(), scope = get()) }
}

private class AppInfoProviderImpl(application: Application) : AppInfoProvider {
  override val packageName: String = application.packageName
  override val buildCommit: String = BuildConfig.COMMIT
  override val receiveBroadcastsPermission: String = "${packageName}.permission.RECEIVE_BROADCASTS"
  override val mainActivityClass: KClass<*> = MainActivity::class
  override val restartReceiverClass: KClass<*> = RestartReceiver::class
  override val mainActivityAlias: ComponentName = application.run {
    val mainActivityName = appInfoProvider.mainActivityClass.java.name
    val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val resolveFlags = PackageManager.MATCH_DISABLED_COMPONENTS

    packageManager.queryIntentActivitiesCompat(launcherIntent, resolveFlags).firstNotNullOfOrNull {
      resolveInfo ->
      val activityInfo = resolveInfo.activityInfo
      if (activityInfo.targetActivity == mainActivityName) {
        ComponentName(activityInfo.packageName, activityInfo.name)
      } else {
        null
      }
    } ?: error("Launcher alias targeting $mainActivityName is not declared in AndroidManifest.xml")
  }
}
