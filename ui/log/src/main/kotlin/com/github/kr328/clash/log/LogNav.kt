package com.github.kr328.clash.log

import androidx.compose.runtime.remember
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import com.github.kr328.clash.log.ui.LogcatScreen
import com.github.kr328.clash.log.ui.LogsScreen
import com.github.kr328.clash.ui.nav.MihomoNavDisplay
import com.github.kr328.clash.ui.nav.addIfNotLast
import kotlinx.serialization.Serializable

sealed interface LogRoute : NavKey {
  @Serializable data object Root : LogRoute
}

@Serializable private data object Logs : LogRoute

@Serializable private data class Logcat(val fileName: String? = null) : LogRoute

fun EntryProviderScope<NavKey>.logsEntries() {
  entry<LogRoute.Root> {
    val initial = remember { if (LogcatService.running.value) Logcat() else Logs }
    val backStack = rememberNavBackStack(initial)
    MihomoNavDisplay(
      backStack = backStack,
      entryProvider =
        entryProvider {
          entry<Logs> {
            LogsScreen(
              onStartLogcat = {
                backStack.removeLastOrNull()
                backStack.addIfNotLast(Logcat())
              },
              onOpenFile = { file -> backStack.addIfNotLast(Logcat(file.fileName)) },
            )
          }
          entry<Logcat> { key ->
            LogcatScreen(
              fileName = key.fileName,
              onOpenLogs = {
                backStack.removeLastOrNull()
                backStack.addIfNotLast(Logs)
              },
              onInvalidFile = { backStack.removeLastOrNull() },
              onClose = { backStack.removeLastOrNull() },
            )
          }
        },
    )
  }
}
