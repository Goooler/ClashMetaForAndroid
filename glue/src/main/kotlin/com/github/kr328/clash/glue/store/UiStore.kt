package com.github.kr328.clash.glue.store

import android.app.Application
import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import com.github.kr328.clash.common.di.AppInfoProvider
import com.github.kr328.clash.common.store.Store
import com.github.kr328.clash.common.store.asStoreProvider
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.glue.model.AppInfo
import com.github.kr328.clash.glue.model.DarkMode
import kotlin.LazyThreadSafetyMode.NONE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class UiStore(
  application: Application,
  private val scope: CoroutineScope,
  appInfoProvider: AppInfoProvider,
) {
  private val preferences = application.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
  private val store = Store(preferences.asStoreProvider())

  val valueState: StateFlow<ValueState> by
    lazy(NONE) {
      val readValues = {
        ValueState(
          enableVpn = enableVpn,
          darkMode = darkMode,
          hideAppIcon = hideAppIcon,
          hideFromRecents = hideFromRecents,
          proxyExcludeNotSelectable = proxyExcludeNotSelectable,
          proxyLine = proxyLine,
          proxySort = proxySort,
          proxyLastGroup = proxyLastGroup,
          accessControlSort = accessControlSort,
          accessControlReverse = accessControlReverse,
          accessControlSystemApp = accessControlSystemApp,
        )
      }
      callbackFlow {
          val listener = OnSharedPreferenceChangeListener { _, _ -> trySend(readValues()) }
          preferences.registerOnSharedPreferenceChangeListener(listener)
          trySend(readValues())
          awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
        }
        .stateIn(
          scope = scope,
          started = SharingStarted.WhileSubscribed(),
          initialValue = readValues(),
        )
    }

  var enableVpn: Boolean by store.boolean(key = "enable_vpn", defaultValue = true)

  var darkMode: DarkMode by
    store.enum(key = "dark_mode", defaultValue = Auto, values = DarkMode.entries.toTypedArray())

  var hideAppIcon: Boolean by
    store.boolean(
      key = "hide_app_icon",
      defaultValue =
        application.packageManager
          .getComponentEnabledSetting(appInfoProvider.mainActivityAlias)
          .let { state ->
            state != PackageManager.COMPONENT_ENABLED_STATE_ENABLED &&
              state != PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
          },
    )

  var hideFromRecents: Boolean by store.boolean(key = "hide_from_recents", defaultValue = false)

  var proxyExcludeNotSelectable by
    store.boolean(key = "proxy_exclude_not_selectable", defaultValue = false)

  var proxyLine: Int by store.int(key = "proxy_line", defaultValue = 2)

  var proxySort: ProxySort by
    store.enum(
      key = "proxy_sort",
      defaultValue = Default,
      values = ProxySort.entries.toTypedArray(),
    )

  var proxyLastGroup: String by store.string(key = "proxy_last_group", defaultValue = "")

  var accessControlSort: AppInfo.Sorter by
    store.enum(
      key = "access_control_sort",
      defaultValue = Label,
      values = AppInfo.Sorter.entries.toTypedArray(),
    )

  var accessControlReverse: Boolean by
    store.boolean(key = "access_control_reverse", defaultValue = false)

  var accessControlSystemApp: Boolean by
    store.boolean(key = "access_control_system_app", defaultValue = false)

  data class ValueState(
    val enableVpn: Boolean,
    val darkMode: DarkMode,
    val hideAppIcon: Boolean,
    val hideFromRecents: Boolean,
    val proxyExcludeNotSelectable: Boolean,
    val proxyLine: Int,
    val proxySort: ProxySort,
    val proxyLastGroup: String,
    val accessControlSort: AppInfo.Sorter,
    val accessControlReverse: Boolean,
    val accessControlSystemApp: Boolean,
  )

  companion object : KoinComponent {
    private const val PREFERENCE_NAME = "ui"

    val uiStore: UiStore = get()
  }
}
