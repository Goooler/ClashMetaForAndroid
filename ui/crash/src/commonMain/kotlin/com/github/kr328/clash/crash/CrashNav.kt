package com.github.kr328.clash.crash

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface CrashRoute : NavKey {
  @Serializable data object ApkBroken : CrashRoute

  @Serializable data object AppCrashed : CrashRoute
}

expect fun EntryProviderScope<NavKey>.crashEntries()
