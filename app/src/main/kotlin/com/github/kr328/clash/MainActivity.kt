package com.github.kr328.clash

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.util.unsafeLazy
import com.github.kr328.clash.crash.crashEntries
import com.github.kr328.clash.deeplink.InstallConfigDeepLink
import com.github.kr328.clash.deeplink.MainDeepLinkParser
import com.github.kr328.clash.deeplink.MainNavigationAction
import com.github.kr328.clash.log.LogRoute
import com.github.kr328.clash.log.logsEntries
import com.github.kr328.clash.main.MainRoute
import com.github.kr328.clash.main.mainEntries
import com.github.kr328.clash.nav.MihomoNavDisplay
import com.github.kr328.clash.nav.addIfNotLast
import com.github.kr328.clash.profile.ProfilesRoute
import com.github.kr328.clash.profile.profilesEntries
import com.github.kr328.clash.proxy.ProxyRoute
import com.github.kr328.clash.proxy.proxyEntries
import com.github.kr328.clash.remote.Remote
import com.github.kr328.clash.service.model.Profile
import com.github.kr328.clash.settings.SettingsRoute
import com.github.kr328.clash.settings.settingsEntries
import com.github.kr328.clash.store.UiStore
import com.github.kr328.clash.ui.theme.MihomoTheme
import com.github.kr328.clash.util.startClashService
import com.github.kr328.clash.util.stopClashService
import com.github.kr328.clash.util.toast
import com.github.kr328.clash.util.withProfile
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  private val uiStore by unsafeLazy { UiStore(this) }
  private val viewModel: ActivityViewModel by
    viewModels(factoryProducer = { ActivityViewModel.Factory(this@MainActivity) })
  private inline val backStack
    get() = viewModel.backStack

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (intent.handleExternalQuickAction()) {
      finish()
      return
    }
    intent.handleNavigationAction(backStack)

    enableEdgeToEdge()
    // TODO: https://issuetracker.google.com/issues/298296168
    //  Fix for three-button nav not properly going edge-to-edge.
    ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets -> insets }

    setContent {
      MihomoTheme {
        MihomoNavDisplay(
          backStack = backStack,
          entryProvider =
            entryProvider {
              mainEntries(
                onOpenProxy = { backStack.addIfNotLast(ProxyRoute.Proxy) },
                onOpenProfiles = { backStack.addIfNotLast(ProfilesRoute.Profiles()) },
                onOpenProviders = { backStack.addIfNotLast(ProfilesRoute.Providers) },
                onOpenLogs = { backStack.addIfNotLast(LogRoute.Root) },
                onOpenSettings = { backStack.addIfNotLast(SettingsRoute.Root) },
                onOpenHelp = { backStack.addIfNotLast(MainRoute.Help) },
              )
              proxyEntries {
                backStack.clear()
                backStack.add(MainRoute.Main)
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
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    if (intent.handleExternalQuickAction()) return
    intent.handleNavigationAction(backStack)
  }

  private fun Intent.handleNavigationAction(backStack: MutableList<NavKey>) {
    when (val route = MainDeepLinkParser.parse(this)) {
      is MainNavigationAction.InstallConfig -> viewModel.handleInstallConfigDeepLink(route.deepLink)
      is MainNavigationAction.Navigate -> {
        if (route.resetBackStack) {
          backStack.clear()
          backStack.add(route.key)
        } else {
          backStack.addIfNotLast(route.key)
        }
      }
      null -> Unit
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
      toast(R.string.unable_to_start_vpn)
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

  private class ActivityViewModel(application: Application) : AndroidViewModel(application) {
    val backStack = mutableStateListOf<NavKey>(MainRoute.Main)

    fun handleInstallConfigDeepLink(deepLink: InstallConfigDeepLink) {
      viewModelScope.launch {
        val uuid = withProfile {
          val type =
            when (deepLink.type?.lowercase()) {
              "url" -> Profile.Type.Url
              "file" -> Profile.Type.File
              else -> Profile.Type.Url
            }
          val name = deepLink.name ?: application.getString(R.string.new_profile)
          create(type, name).also { patch(it, name, deepLink.url, 0) }
        }
        backStack.addIfNotLast(ProfilesRoute.Profiles(openPropertyUuid = uuid))
      }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ActivityViewModel(application = context.applicationContext as Application) as T
    }
  }

  companion object {
    fun intent(context: Context, action: String? = null) =
      Intent(context, MainActivity::class.java).setAction(action)
  }
}
