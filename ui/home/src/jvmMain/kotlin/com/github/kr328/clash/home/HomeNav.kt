package com.github.kr328.clash.home

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

actual fun EntryProviderScope<NavKey>.homeEntries(
  onOpenProxy: () -> Unit,
  onOpenProfiles: () -> Unit,
  onOpenProviders: () -> Unit,
  onOpenLogs: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenHelp: () -> Unit,
) = Unit
