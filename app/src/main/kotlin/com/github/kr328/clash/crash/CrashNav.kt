package com.github.kr328.clash.crash

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.kr328.clash.crash.ui.ApkBrokenScreen
import com.github.kr328.clash.crash.ui.AppCrashedScreen
import kotlinx.serialization.Serializable

sealed interface CrashRoute : NavKey {
  @Serializable data object ApkBroken : CrashRoute

  @Serializable data object AppCrashed : CrashRoute
}

fun EntryProviderScope<NavKey>.crashEntries() {
  entry<CrashRoute.ApkBroken> { ApkBrokenScreen() }
  entry<CrashRoute.AppCrashed> { AppCrashedScreen() }
}
