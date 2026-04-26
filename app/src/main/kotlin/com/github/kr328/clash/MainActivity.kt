package com.github.kr328.clash

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.ActivityManager
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.util.unsafeLazy
import com.github.kr328.clash.common.util.uuid
import com.github.kr328.clash.crash.CrashRoute
import com.github.kr328.clash.crash.crashEntries
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
import com.github.kr328.clash.settings.SettingsRoute
import com.github.kr328.clash.settings.settingsEntries
import com.github.kr328.clash.store.UiStore
import com.github.kr328.clash.ui.theme.MihomoTheme

class MainActivity : ComponentActivity() {
  private val uiStore by unsafeLazy { UiStore(this) }
  private val viewModel: ActivityViewModel by
    viewModels(factoryProducer = { ActivityViewModel.Factory })
  private inline val backStack
    get() = viewModel.backStack

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    intent.handleAction(backStack)

    enableEdgeToEdge()
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
    intent.handleAction(backStack)
  }

  private fun Intent.handleAction(backStack: MutableList<NavKey>) {
    when (action) {
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

  /** Aims for retaining [backStack]. */
  private class ActivityViewModel : ViewModel() {
    val backStack = mutableStateListOf<NavKey>(MainRoute.Main)

    companion object Factory : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T = ActivityViewModel() as T
    }
  }

  companion object {
    fun intent(context: Context, action: String? = null) =
      Intent(context, MainActivity::class.java).setAction(action)
  }
}
