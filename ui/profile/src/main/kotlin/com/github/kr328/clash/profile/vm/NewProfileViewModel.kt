package com.github.kr328.clash.profile.vm

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.glue.util.withProfile
import com.github.kr328.clash.profile.R
import com.github.kr328.clash.profile.model.ProfileProvider
import com.github.kr328.clash.service.model.Profile
import io.github.g00fy2.quickie.QRResult
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class NewProfileViewModel(app: Application) : AndroidViewModel(app) {
  val uiState: StateFlow<UiState>
    field = MutableStateFlow(UiState())

  val eventState: StateFlow<EventState>
    field = MutableStateFlow<EventState>(EventState.Idle)

  init {
    loadProviders()
  }

  fun consumeEvent() {
    eventState.value = EventState.Idle
  }

  fun onCreate(provider: ProfileProvider) {
    when (provider) {
      is QR -> eventState.value = EventState.LaunchQRScanner
      is External -> eventState.value = EventState.LaunchExternalProvider(provider.intent)
      is File -> createProfile(File)
      is Url -> createProfile(Url)
    }
  }

  fun onDetail(provider: ProfileProvider.External) {
    val packageName = provider.intent.component?.packageName ?: return
    val uri = Uri.fromParts("package", packageName, null)
    eventState.value = EventState.OpenAppSettings(uri)
  }

  fun onExternalProviderResult(uri: Uri, name: String?) {
    viewModelScope.launch {
      try {
        val profileName = application.getString(CommonR.string.new_profile)
        val uuid = withProfile { create(External, name ?: profileName, uri.toString()) }
        eventState.value = EventState.LaunchProperties(uuid)
      } catch (e: Exception) {
        Log.e("Create external profile failed: ${e.message}", e)
        eventState.value =
          EventState.ShowMessage(e.message ?: application.getString(CommonR.string.unknown))
      }
    }
  }

  fun onQRResult(result: QRResult) {
    when (result) {
      is QRSuccess -> {
        val url = result.content.rawValue ?: result.content.rawBytes?.let { String(it) }.orEmpty()
        viewModelScope.launch {
          try {
            val uuid = withProfile {
              create(type = Url, name = application.getString(CommonR.string.new_profile), url)
            }
            eventState.value = EventState.LaunchProperties(uuid)
          } catch (e: Exception) {
            Log.e("Create QR profile failed: ${e.message}", e)
            eventState.value =
              EventState.ShowMessage(e.message ?: application.getString(CommonR.string.unknown))
          }
        }
      }
      QRUserCanceled -> Unit
      QRMissingPermission ->
        eventState.value =
          EventState.ShowMessage(application.getString(R.string.import_from_qr_no_permission))
      is QRError ->
        eventState.value =
          EventState.ShowMessage(application.getString(R.string.import_from_qr_exception))
    }
  }

  private fun createProfile(type: Profile.Type) {
    viewModelScope.launch {
      try {
        val name = application.getString(CommonR.string.new_profile)
        val uuid = withProfile { create(type, name) }
        eventState.value = EventState.LaunchProperties(uuid)
      } catch (e: Exception) {
        Log.e("Create profile failed: ${e.message}", e)
        eventState.value =
          EventState.ShowMessage(e.message ?: application.getString(CommonR.string.unknown))
      }
    }
  }

  private fun loadProviders() {
    viewModelScope.launch {
      val providers =
        withContext(Dispatchers.IO) {
          val externalProviders =
            application.packageManager
              .queryIntentActivities(Intent(Intents.ACTION_PROVIDE_URL), 0)
              .map {
                val activity = it.activityInfo
                val name = activity.applicationInfo.loadLabel(application.packageManager)
                val summary = activity.loadLabel(application.packageManager)
                val icon = activity.loadIcon(application.packageManager)
                val intent =
                  Intent(Intents.ACTION_PROVIDE_URL)
                    .setComponent(ComponentName(activity.packageName, activity.name))
                ProfileProvider.External(name.toString(), summary.toString(), icon, intent)
              }

          listOf(
            ProfileProvider.File(application),
            ProfileProvider.Url(application),
            ProfileProvider.QR(application),
          ) + externalProviders
        }
      uiState.update { it.copy(providers = providers) }
    }
  }

  data class UiState(val providers: List<ProfileProvider> = emptyList())

  sealed interface EventState {
    data object Idle : EventState

    data object LaunchQRScanner : EventState

    data class LaunchExternalProvider(val intent: Intent) : EventState

    data class LaunchProperties(val uuid: Uuid) : EventState

    data class OpenAppSettings(val uri: Uri) : EventState

    data class ShowMessage(val message: String) : EventState

    data object Finish : EventState
  }
}
