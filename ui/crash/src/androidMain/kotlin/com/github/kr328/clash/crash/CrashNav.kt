package com.github.kr328.clash.crash

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.kr328.clash.crash.ui.ApkBrokenScreen
import com.github.kr328.clash.crash.ui.AppCrashedScreen

actual fun EntryProviderScope<NavKey>.crashEntries() {
  entry<CrashRoute.ApkBroken> { ApkBrokenScreen() }
  entry<CrashRoute.AppCrashed> { AppCrashedScreen() }
}
