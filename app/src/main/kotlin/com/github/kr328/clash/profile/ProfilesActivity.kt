package com.github.kr328.clash.profile

import com.github.kr328.clash.R
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.common.util.setUUID
import com.github.kr328.clash.common.util.ticker
import com.github.kr328.clash.profile.ui.ProfilesDesign
import com.github.kr328.clash.service.model.Profile
import com.github.kr328.clash.ui.DesignActivity
import com.github.kr328.clash.ui.SnackbarDuration
import com.github.kr328.clash.util.withProfile
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext

class ProfilesActivity : DesignActivity<ProfilesDesign>() {
  override suspend fun main() {
    val design = ProfilesDesign(this)

    setContentDesign(design)

    val ticker = ticker(TimeUnit.MINUTES.toMillis(1))

    while (isActive) {
      select {
        events.onReceive {
          when (it) {
            Event.ActivityStart,
            Event.ProfileChanged -> {
              design.fetch()
            }
            is Event.ProfileUpdateCompleted -> {
              it.uuid?.let(::showProfileUpdateCompleted)
            }
            is Event.ProfileUpdateFailed -> {
              it.uuid?.let { uuid -> showProfileUpdateFailed(uuid, it.reason) }
            }
            else -> Unit
          }
        }
        design.requests.onReceive {
          when (it) {
            ProfilesDesign.Request.Create -> startActivity(NewProfileActivity::class.intent)
            ProfilesDesign.Request.UpdateAll ->
              withProfile {
                try {
                  queryAll().forEach { p ->
                    if (p.imported && p.type != Profile.Type.File) update(p.uuid)
                  }
                } finally {
                  withContext(Dispatchers.Main) { design.finishUpdateAll() }
                }
              }
            is ProfilesDesign.Request.Update -> withProfile { update(it.profile.uuid) }
            is ProfilesDesign.Request.Delete -> withProfile { delete(it.profile.uuid) }
            is ProfilesDesign.Request.Edit ->
              startActivity(PropertiesActivity::class.intent.setUUID(it.profile.uuid))
            is ProfilesDesign.Request.Active -> {
              withProfile {
                if (it.profile.imported) setActive(it.profile) else design.requestSave(it.profile)
              }
            }
            is ProfilesDesign.Request.Duplicate -> {
              val uuid = withProfile { clone(it.profile.uuid) }

              startActivity(PropertiesActivity::class.intent.setUUID(uuid))
            }
          }
        }
        if (activityStarted) {
          ticker.onReceive { design.updateElapsed() }
        }
      }
    }
  }

  private suspend fun ProfilesDesign.fetch() {
    withProfile { patchProfiles(queryAll()) }
  }

  private fun showProfileUpdateCompleted(uuid: UUID) {
    launch {
      var name: String? = null
      withProfile { name = queryByUUID(uuid)?.name }
      design?.snackbar(
        getString(R.string.toast_profile_updated_complete, name),
        SnackbarDuration.Long,
      )
    }
  }

  private fun showProfileUpdateFailed(uuid: UUID, reason: String?) {
    launch {
      var name: String? = null
      withProfile { name = queryByUUID(uuid)?.name }
      design?.snackbar(
        getString(R.string.toast_profile_updated_failed, name, reason),
        SnackbarDuration.Long,
      ) {
        setAction(R.string.edit) { startActivity(PropertiesActivity::class.intent.setUUID(uuid)) }
      }
    }
  }
}
