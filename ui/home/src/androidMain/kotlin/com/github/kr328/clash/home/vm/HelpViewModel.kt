package com.github.kr328.clash.home.vm

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.di.AppInfoProvider
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.bridge.Bridge
import com.github.kr328.clash.glue.util.TABBY_RELEASES_LATEST
import com.github.kr328.clash.home.R
import com.github.kr328.clash.home.already_up_to_date
import com.github.kr328.clash.home.api.HelpApi
import com.github.kr328.clash.home.check_update_failed
import com.github.kr328.clash.ui.util.getString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.swiftzer.semver.SemVer

internal class HelpViewModel(
  private val application: Application,
  private val appInfoProvider: AppInfoProvider,
) : ViewModel() {
  private val api = HelpApi()

  val uiState: StateFlow<UiState>
    field = MutableStateFlow(UiState())

  val eventState: StateFlow<EventState>
    field = MutableStateFlow<EventState>(EventState.Idle)

  init {
    loadVersionInfo()
  }

  fun checkForUpdates() {
    if (uiState.value.checkingForUpdates) return

    viewModelScope.launch {
      uiState.update { it.copy(checkingForUpdates = true) }
      try {
        val latestTag = api.getLatestRelease()

        if (latestTag == null) {
          eventState.update {
            EventState.ShowMessage(application.getString(R.string.check_update_failed))
          }
          return@launch
        }

        val localVersion =
          application.packageManager.getPackageInfo(application.packageName, 0).versionName ?: ""
        if (SemVer.parse(latestTag) > SemVer.parse(localVersion)) {
          eventState.update { EventState.UpdateAvailable(TABBY_RELEASES_LATEST) }
        } else {
          eventState.update {
            EventState.ShowMessage(application.getString(R.string.already_up_to_date))
          }
        }
      } catch (e: Exception) {
        Log.e("Check for updates failed: ${e.message}", e)
        eventState.update {
          EventState.ShowMessage(application.getString(R.string.check_update_failed))
        }
      } finally {
        uiState.update { it.copy(checkingForUpdates = false) }
      }
    }
  }

  fun consumeEvent() {
    eventState.value = EventState.Idle
  }

  private fun loadVersionInfo() {
    viewModelScope.launch {
      val (appVersion, coreVersion) =
        withContext(Dispatchers.IO) {
          val pkgInfo = application.packageManager.getPackageInfo(application.packageName, 0)
          "${pkgInfo.versionName} - ${appInfoProvider.buildCommit}" to Bridge.nativeCoreVersion()
        }

      uiState.update { it.copy(appVersion = appVersion, coreVersion = coreVersion) }
    }
  }

  data class UiState(
    val checkingForUpdates: Boolean = false,
    val appVersion: String = "",
    val coreVersion: String = "",
  )

  sealed interface EventState {
    data object Idle : EventState

    data class ShowMessage(val message: String) : EventState

    data class UpdateAvailable(val releasesUrl: String) : EventState
  }
}
