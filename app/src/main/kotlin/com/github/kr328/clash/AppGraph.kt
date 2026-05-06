package com.github.kr328.clash

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph

@DependencyGraph(AppScope::class)
interface AppGraph {
  val mainActivityClassProvider: MainActivityClassProvider
}
