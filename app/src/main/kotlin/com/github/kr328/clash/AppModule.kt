package com.github.kr328.clash

import android.app.Activity
import com.github.kr328.clash.util.MainActivityClassProvider
import org.koin.dsl.module

val appModule = module { single<MainActivityClassProvider> { MainActivityClassProviderImpl } }

private object MainActivityClassProviderImpl : MainActivityClassProvider {
  override val mainActivityClass: Class<out Activity> = MainActivity::class.java
}
