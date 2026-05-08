package com.github.kr328.clash.app

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color.TRANSPARENT
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.application
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.util.unsafeLazy
import com.github.kr328.clash.common.util.uuid
import com.github.kr328.clash.crash.CrashRoute
import com.github.kr328.clash.crash.crashEntries
import com.github.kr328.clash.glue.model.DarkMode
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.store.UiStore
import com.github.kr328.clash.glue.util.mainIntent
import com.github.kr328.clash.glue.util.startClashService
import com.github.kr328.clash.glue.util.stopClashService
import com.github.kr328.clash.glue.util.withProfile
import com.github.kr328.clash.home.HomeRoute
import com.github.kr328.clash.home.homeEntries
import com.github.kr328.clash.log.LogRoute
import com.github.kr328.clash.log.logsEntries
import com.github.kr328.clash.profile.ProfilesRoute
import com.github.kr328.clash.profile.profilesEntries
import com.github.kr328.clash.proxy.ProxyRoute
import com.github.kr328.clash.proxy.proxyEntries
import com.github.kr328.clash.service.model.Profile
import com.github.kr328.clash.settings.SettingsRoute
import com.github.kr328.clash.settings.settingsEntries
import com.github.kr328.clash.ui.nav.TabbyNavDisplay
import com.github.kr328.clash.ui.nav.addIfNotLast
import com.github.kr328.clash.ui.theme.TabbyTheme
import java.util.Locale
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  private val uiStore by unsafeLazy { UiStore(this) }
  private val viewModel: ViewModel by
    viewModels(factoryProducer = { ViewModel.Factory(this@MainActivity) })
  private inline val backStack
    get() = viewModel.backStack

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (intent.handleExternalQuickAction()) {
      finish()
      return
    }
    intent.handleAction(backStack)

    setContent {
      val uiValueState by uiStore.valueState.collectAsStateWithLifecycle()
      val darkMode = uiValueState.darkMode

      LaunchedEffect(darkMode) { edgeToEdge(darkMode) }

      TabbyTheme(darkModeInSettings = darkMode) {
        TabbyNavDisplay(
          backStack = backStack,
          entryProvider =
            entryProvider {
              homeEntries(
                onOpenProxy = { backStack.addIfNotLast(ProxyRoute.Proxy) },
                onOpenProfiles = { backStack.addIfNotLast(ProfilesRoute.Profiles()) },
                onOpenProviders = { backStack.addIfNotLast(ProfilesRoute.Providers) },
                onOpenLogs = { backStack.addIfNotLast(LogRoute.Root) },
                onOpenSettings = { backStack.addIfNotLast(SettingsRoute.Root) },
                onOpenHelp = { backStack.addIfNotLast(HomeRoute.Help) },
              )
              proxyEntries {
                backStack.clear()
                backStack.add(HomeRoute.Home)
              }
              profilesEntries()
              logsEntries()
              settingsEntries()
              crashEntries()
            },
        )
      }
    }

    requestNotificationPermission()
    setExcludeFromRecents()
    setupShortcuts()
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    if (intent.handleExternalQuickAction()) return
    intent.handleAction(backStack)
  }

  private fun Intent.handleAction(backStack: MutableList<NavKey>) {
    when (action) {
      Intent.ACTION_VIEW -> {
        val uri = data ?: return
        viewModel.handleInstallConfigUri(uri)
      }
      Intents.ACTION_PROPERTIES -> {
        uuid?.let { uuid ->
          backStack.addIfNotLast(ProfilesRoute.Profiles(openPropertyUuid = uuid))
        }
      }
      Intents.ACTION_LOGCAT -> backStack.addIfNotLast(LogRoute.Root)
      Intents.ACTION_APP_CRASHED -> {
        backStack.clear()
        backStack.add(CrashRoute.AppCrashed)
      }
      Intents.ACTION_APK_BROKEN -> {
        backStack.clear()
        backStack.add(CrashRoute.ApkBroken)
      }
    }
  }

  private fun Intent.handleExternalQuickAction(): Boolean {
    return when (action) {
      Intents.ACTION_TOGGLE_CLASH -> {
        if (Remote.broadcasts.clashRunning) stopClash() else startClash()
        true
      }
      Intents.ACTION_START_CLASH -> {
        if (!Remote.broadcasts.clashRunning) startClash()
        else toast(R.string.external_control_started)
        true
      }
      Intents.ACTION_STOP_CLASH -> {
        if (Remote.broadcasts.clashRunning) stopClash()
        else toast(R.string.external_control_stopped)
        true
      }
      else -> false
    }
  }

  private fun startClash() {
    val vpnRequest = startClashService()
    if (vpnRequest != null) {
      toast(CommonR.string.unable_to_start_vpn)
      return
    }
    toast(R.string.external_control_started)
  }

  private fun stopClash() {
    stopClashService()
    toast(R.string.external_control_stopped)
  }

  private fun requestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (
        ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) !=
          PackageManager.PERMISSION_GRANTED
      ) {
        registerForActivityResult(RequestPermission(), callback = {}).launch(POST_NOTIFICATIONS)
      }
    }
  }

  private fun setExcludeFromRecents() {
    checkNotNull(getSystemService<ActivityManager>()).appTasks.forEach { task ->
      task.setExcludeFromRecents(uiStore.hideFromRecents)
    }
  }

  private fun edgeToEdge(darkMode: DarkMode) {
    val systemBars =
      when (darkMode) {
        ForceDark -> SystemBarStyle.auto(TRANSPARENT, TRANSPARENT) { true }
        ForceLight -> SystemBarStyle.auto(TRANSPARENT, TRANSPARENT) { false }
        Auto -> SystemBarStyle.auto(TRANSPARENT, TRANSPARENT)
      }
    enableEdgeToEdge(statusBarStyle = systemBars, navigationBarStyle = systemBars)
    // TODO: https://issuetracker.google.com/issues/298296168
    //  Fix for three-button nav not properly going edge-to-edge.
    ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets -> insets }
  }

  private fun setupShortcuts() {
    // Prevent launcher activity not found.
    if (uiStore.hideAppIcon) return

    val icon = IconCompat.createWithResource(this, R.mipmap.ic_launcher)
    val flags =
      Intent.FLAG_ACTIVITY_NEW_TASK or
        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
        Intent.FLAG_ACTIVITY_NO_ANIMATION

    val toggle =
      ShortcutInfoCompat.Builder(this, "toggle_clash")
        .setShortLabel(getString(R.string.shortcut_toggle_short))
        .setLongLabel(getString(R.string.shortcut_toggle_long))
        .setIcon(icon)
        .setIntent(mainIntent(action = Intents.ACTION_TOGGLE_CLASH).addFlags(flags))
        .setRank(0)
        .build()

    val start =
      ShortcutInfoCompat.Builder(this, "start_clash")
        .setShortLabel(getString(R.string.shortcut_start_short))
        .setLongLabel(getString(R.string.shortcut_start_long))
        .setIcon(icon)
        .setIntent(mainIntent(action = Intents.ACTION_START_CLASH).addFlags(flags))
        .setRank(1)
        .build()

    val stop =
      ShortcutInfoCompat.Builder(this, "stop_clash")
        .setShortLabel(getString(R.string.shortcut_stop_short))
        .setLongLabel(getString(R.string.shortcut_stop_long))
        .setIcon(icon)
        .setIntent(mainIntent(action = Intents.ACTION_STOP_CLASH).addFlags(flags))
        .setRank(2)
        .build()

    ShortcutManagerCompat.setDynamicShortcuts(this, listOf(toggle, start, stop))
  }

  private class ViewModel(application: Application) : AndroidViewModel(application) {
    val backStack = mutableStateListOf<NavKey>(HomeRoute.Home)

    fun handleInstallConfigUri(uri: Uri) {
      val url = uri.getQueryParameter("url") ?: return
      viewModelScope.launch {
        val uuid = withProfile {
          val type =
            when (uri.getQueryParameter("type")?.lowercase(Locale.getDefault())) {
              "url" -> Profile.Type.Url
              "file" -> Profile.Type.File
              else -> Profile.Type.Url
            }
          val name =
            uri.getQueryParameter("name") ?: application.getString(CommonR.string.new_profile)
          create(type, name).also { patch(it, name, url, 0) }
        }
        backStack.addIfNotLast(ProfilesRoute.Profiles(openPropertyUuid = uuid))
      }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
        ViewModel(application = context.applicationContext as Application) as T
    }
  }
}

private fun Activity.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_LONG) {
  Toast.makeText(this, resId, duration).show()
}
