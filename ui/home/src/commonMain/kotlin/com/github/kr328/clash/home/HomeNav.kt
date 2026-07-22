package com.github.kr328.clash.home

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.kr328.clash.home.ui.HelpScreen
import com.github.kr328.clash.home.ui.HomeScreen
import kotlinx.serialization.Serializable

sealed interface HomeRoute : NavKey {
  @Serializable data object Home : HomeRoute

  @Serializable data object Help : HomeRoute
}

fun EntryProviderScope<NavKey>.homeEntries(
  onOpenProxy: () -> Unit,
  onOpenProfiles: () -> Unit,
  onOpenProviders: () -> Unit,
  onOpenLogs: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenHelp: () -> Unit,
) {
  entry<HomeRoute.Home> {
    HomeScreen(
      onOpenProxy = onOpenProxy,
      onOpenProfiles = onOpenProfiles,
      onOpenProviders = onOpenProviders,
      onOpenLogs = onOpenLogs,
      onOpenSettings = onOpenSettings,
      onOpenHelp = onOpenHelp,
    )
  }
  entry<HomeRoute.Help> { HelpScreen() }
}
