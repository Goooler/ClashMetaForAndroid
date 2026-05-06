package com.github.kr328.clash

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
object MainActivityClassProviderImpl : MainActivityClassProvider {
  override val mainActivityClass: Class<*> = MainActivity::class.java
}
