package com.github.kr328.clash.main

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.kr328.clash.main.ui.HelpScreen
import com.github.kr328.clash.main.ui.MainScreen
import kotlinx.serialization.Serializable

sealed interface MainRoute : NavKey {
  @Serializable data object Main : MainRoute

  @Serializable data object Help : MainRoute
}

fun EntryProviderScope<NavKey>.mainEntries(
  onOpenProxy: () -> Unit,
  onOpenProfiles: () -> Unit,
  onOpenProviders: () -> Unit,
  onOpenLogs: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenHelp: () -> Unit,
) {
  entry<MainRoute.Main> {
    MainScreen(
      onOpenProxy = onOpenProxy,
      onOpenProfiles = onOpenProfiles,
      onOpenProviders = onOpenProviders,
      onOpenLogs = onOpenLogs,
      onOpenSettings = onOpenSettings,
      onOpenHelp = onOpenHelp,
    )
  }
  entry<MainRoute.Help> { HelpScreen() }
}
