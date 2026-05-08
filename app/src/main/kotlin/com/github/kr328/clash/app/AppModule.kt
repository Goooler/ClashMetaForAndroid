package com.github.kr328.clash.app

import android.app.Activity
import com.github.kr328.clash.glue.di.AppInfoProvider
import org.koin.dsl.module

val appModule = module { single<AppInfoProvider> { AppInfoProviderImpl } }

private object AppInfoProviderImpl : AppInfoProvider {
  override val mainActivityClass: Class<out Activity> = MainActivity::class.java
}
