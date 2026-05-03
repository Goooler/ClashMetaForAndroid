package com.github.kr328.clash.proxy

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.kr328.clash.proxy.ui.ProxyScreen
import kotlinx.serialization.Serializable

sealed interface ProxyRoute : NavKey {
  @Serializable data object Proxy : ProxyRoute
}

fun EntryProviderScope<NavKey>.proxyEntries(onReLaunch: () -> Unit) {
  entry<ProxyRoute.Proxy> { ProxyScreen(onReLaunch = onReLaunch) }
}
