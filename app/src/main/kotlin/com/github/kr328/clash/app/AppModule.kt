package com.github.kr328.clash.app

import android.app.Application
import com.github.kr328.clash.common.di.AppInfoProvider
import org.koin.dsl.module

val appModule = module {
  single<AppInfoProvider> { AppInfoProviderImpl(application = get()) }
}

private class AppInfoProviderImpl(application: Application) : AppInfoProvider {
  override val packageName: String = application.packageName
  override val buildCommit: String = BuildConfig.COMMIT
  override val receiveBroadcastsPermission: String = "${packageName}.permission.RECEIVE_BROADCASTS"
  override val mainActivityClass: Class<*> = MainActivity::class.java
  override val restartReceiverClass: Class<*> = RestartReceiver::class.java
}
