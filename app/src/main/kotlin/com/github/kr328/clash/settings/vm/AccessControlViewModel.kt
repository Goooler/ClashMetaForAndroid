package com.github.kr328.clash.settings.vm

import android.Manifest
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Process
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.model.AppInfo
import com.github.kr328.clash.model.AppInfoSort
import com.github.kr328.clash.remote.Remote
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.settings.ui.AccessControlActions
import com.github.kr328.clash.store.UiStore
import com.github.kr328.clash.util.startClashService
import com.github.kr328.clash.util.stopClashService
import com.github.kr328.clash.util.toAppInfo
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class AccessControlViewModel(app: Application) : AndroidViewModel(app), AccessControlActions {
  private val appContext = app
  private val uiStore = UiStore(app)
  private val serviceStore = ServiceStore(app)
  private var reloadAppsJob: Job? = null

  val uiState: StateFlow<UiState>
    field =
      MutableStateFlow(
        UiState(
          apps = emptyList(),
          selected = emptySet(),
          sort = uiStore.accessControlSort,
          reverse = uiStore.accessControlReverse,
          showSystemApps = uiStore.accessControlSystemApp,
        )
      )

  val clashRunning: StateFlow<Boolean> = Remote.broadcasts.clashRunningFlow

  init {
    viewModelScope.launch {
      val selected = withContext(Dispatchers.IO) { serviceStore.accessControlPackages }
      uiState.update { it.copy(selected = selected) }
      reloadApps()
    }
  }

  fun persistSelection() {
    // Intended to use non-viewModel scope as we need the action to be called on disposed.
    CoroutineScope(Dispatchers.IO).launch {
      val selected = uiState.value.selected
      val persistedSelection = serviceStore.accessControlPackages
      val changed = selected != persistedSelection

      if (changed) {
        serviceStore.accessControlPackages = selected
      }
      if (clashRunning.value && changed) {
        appContext.stopClashService()

        val stopped =
          withTimeoutOrNull(10.seconds) {
            clashRunning.first { !it }
            true
          } ?: false

        if (!stopped) {
          // The stop signal may be lost; continue with a best-effort restart path.
          appContext.stopClashService()
        }

        appContext.startClashService()
      }
    }
  }

  override fun toggleApp(packageName: String) {
    uiState.update { state ->
      val selected =
        if (packageName in state.selected) {
          state.selected - packageName
        } else {
          state.selected + packageName
        }

      state.copy(selected = selected)
    }
  }

  override fun selectAll() {
    viewModelScope.launch {
      val all =
        withContext(Dispatchers.Default) { uiState.value.apps.map(AppInfo::packageName).toSet() }
      uiState.update { it.copy(selected = all) }
    }
  }

  override fun selectNone() {
    uiState.update { it.copy(selected = emptySet()) }
  }

  override fun selectInvert() {
    viewModelScope.launch {
      val all =
        withContext(Dispatchers.Default) { uiState.value.apps.map(AppInfo::packageName).toSet() }
      uiState.update { state -> state.copy(selected = all - state.selected) }
    }
  }

  override fun importFromClipboard() {
    val clipboard = appContext.getSystemService<ClipboardManager>()
    val data = clipboard?.primaryClip

    if (data != null && data.itemCount > 0) {
      val packages =
        data
          .getItemAt(0)
          .text
          ?.toString()
          ?.lineSequence()
          ?.map { it.trim() }
          ?.filter { it.isNotEmpty() }
          ?.toSet()
          .orEmpty()
      val all = uiState.value.apps.map(AppInfo::packageName).toSet()
      uiState.update { it.copy(selected = all.intersect(packages)) }
    }
  }

  override fun exportToClipboard() {
    val clipboard = appContext.getSystemService<ClipboardManager>()
    val data = ClipData.newPlainText("packages", uiState.value.selected.sorted().joinToString("\n"))
    clipboard?.setPrimaryClip(data)
  }

  override fun updateSort(sort: AppInfoSort) {
    uiStore.accessControlSort = sort
    uiState.update { it.copy(sort = sort) }
    reloadApps()
  }

  override fun updateReverse(reverse: Boolean) {
    uiStore.accessControlReverse = reverse
    uiState.update { it.copy(reverse = reverse) }
    reloadApps()
  }

  override fun updateShowSystemApps(show: Boolean) {
    uiStore.accessControlSystemApp = show
    uiState.update { it.copy(showSystemApps = show) }
    reloadApps()
  }

  private fun reloadApps() {
    reloadAppsJob?.cancel()
    reloadAppsJob = viewModelScope.launch {
      val state = uiState.value
      val apps =
        loadApps(
          selected = state.selected,
          sort = state.sort,
          reverse = state.reverse,
          showSystemApps = state.showSystemApps,
        )

      uiState.update { it.copy(apps = apps) }
    }
  }

  private suspend fun loadApps(
    selected: Set<String>,
    sort: AppInfoSort,
    reverse: Boolean,
    showSystemApps: Boolean,
  ): List<AppInfo> =
    withContext(Dispatchers.IO) {
      val base = compareByDescending<AppInfo> { it.packageName in selected }
      val comparator = if (reverse) base.thenDescending(sort) else base.then(sort)

      val pm = appContext.packageManager
      val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)

      packages
        .asSequence()
        .filter { it.packageName != appContext.packageName }
        .filter { it.applicationInfo != null }
        .filter {
          it.requestedPermissions?.contains(Manifest.permission.INTERNET) == true ||
            it.applicationInfo!!.uid < Process.FIRST_APPLICATION_UID
        }
        .filter { showSystemApps || !it.isSystemApp }
        .map { it.toAppInfo(pm) }
        .sortedWith(comparator)
        .toList()
    }

  private val PackageInfo.isSystemApp: Boolean
    get() = applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM) != 0

  data class UiState(
    val apps: List<AppInfo>,
    val selected: Set<String>,
    val sort: AppInfoSort,
    val reverse: Boolean,
    val showSystemApps: Boolean,
  )
}
