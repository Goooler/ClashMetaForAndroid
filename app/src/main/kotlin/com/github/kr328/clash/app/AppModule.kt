package com.github.kr328.clash.app

import com.github.kr328.clash.glue.di.AppInfoProvider
import org.koin.dsl.module

val appModule = module { single<AppInfoProvider> { AppInfoProviderImpl } }

private object AppInfoProviderImpl : AppInfoProvider {
  override val mainActivityClass: Class<*> = MainActivity::class.java
  override val restartReceiverClass: Class<*> = RestartReceiver::class.java
}
