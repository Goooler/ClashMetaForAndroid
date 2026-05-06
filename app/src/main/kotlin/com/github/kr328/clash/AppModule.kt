package com.github.kr328.clash

import com.github.kr328.clash.MainActivityClassProvider
import org.koin.dsl.module

val appModule = module { single<MainActivityClassProvider> { MainActivityClassProviderImpl } }

private object MainActivityClassProviderImpl : MainActivityClassProvider {
  override val mainActivityClass: Class<*> = MainActivity::class.java
}
