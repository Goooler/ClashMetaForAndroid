package com.github.kr328.clash.profile

import com.github.kr328.clash.R
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.common.util.setUUID
import com.github.kr328.clash.common.util.uuid
import com.github.kr328.clash.files.FilesActivity
import com.github.kr328.clash.profile.ui.PropertiesDesign
import com.github.kr328.clash.service.model.Profile
import com.github.kr328.clash.ui.BaseActivity
import com.github.kr328.clash.util.showExceptionSnackbar
import com.github.kr328.clash.util.withProfile
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

class PropertiesActivity : BaseActivity<PropertiesDesign>() {
  private var canceled: Boolean = false
  private lateinit var original: Profile

  override suspend fun main() {
    setResult(RESULT_CANCELED)

    val uuid = intent.uuid ?: return finish()
    val design = PropertiesDesign(this)

    original = withProfile { queryByUUID(uuid) } ?: return finish()

    design.profile = original

    setContentDesign(design)

    defer {
      canceled = true

      withProfile { release(uuid) }
    }

    while (isActive) {
      select {
        events.onReceive {
          when (it) {
            Event.ActivityStop -> {
              val profile = design.profile

              if (!canceled && profile != original) {
                withProfile { patch(profile.uuid, profile.name, profile.source, profile.interval) }
              }
            }
            Event.ServiceRecreated -> {
              finish()
            }
            else -> Unit
          }
        }
        design.requests.onReceive {
          when (it) {
            PropertiesDesign.Request.BrowseFiles -> {
              startActivity(FilesActivity::class.intent.setUUID(uuid))
            }
            PropertiesDesign.Request.Commit -> {
              design.verifyAndCommit()
            }
          }
        }
      }
    }
  }

  private suspend fun PropertiesDesign.verifyAndCommit() {
    when {
      profile.name.isBlank() -> {
        snackbar(R.string.empty_name)
      }
      profile.type != Profile.Type.File && profile.source.isBlank() -> {
        snackbar(R.string.invalid_url)
      }
      else -> {
        try {
          withProcessing { updateStatus ->
            withProfile {
              patch(profile.uuid, profile.name, profile.source, profile.interval)

              coroutineScope { commit(profile.uuid) { launch { updateStatus(it) } } }
            }
          }

          setResult(RESULT_OK)

          finish()
        } catch (e: Exception) {
          showExceptionSnackbar(e)
        }
      }
    }
  }
}
