package com.github.kr328.clash.app.di

import android.app.Application
import com.github.kr328.clash.app.BuildConfig
import com.github.kr328.clash.app.MainActivity
import com.github.kr328.clash.app.RestartReceiver
import com.github.kr328.clash.common.di.AppInfoProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val appModule = module {
  single<AppInfoProvider> { AppInfoProviderImpl(application = get()) }
  single<CoroutineScope> { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
}

private class AppInfoProviderImpl(application: Application) : AppInfoProvider {
  override val packageName: String = application.packageName
  override val buildCommit: String = BuildConfig.COMMIT
  override val receiveBroadcastsPermission: String = "${packageName}.permission.RECEIVE_BROADCASTS"
  override val mainActivityClass: Class<*> = MainActivity::class.java
  override val restartReceiverClass: Class<*> = RestartReceiver::class.java
}
