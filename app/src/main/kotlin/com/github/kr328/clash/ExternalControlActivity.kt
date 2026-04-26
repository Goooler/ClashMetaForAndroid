package com.github.kr328.clash

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.github.kr328.clash.MainActivity.Companion.intent as mainIntent
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.util.setUUID
import com.github.kr328.clash.remote.Remote
import com.github.kr328.clash.service.model.Profile
import com.github.kr328.clash.util.startClashService
import com.github.kr328.clash.util.stopClashService
import com.github.kr328.clash.util.toast
import com.github.kr328.clash.util.withProfile
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ExternalControlActivity : Activity(), CoroutineScope by MainScope() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    @Suppress("DEPRECATION") overridePendingTransition(0, 0)

    when (intent.action) {
      Intent.ACTION_VIEW -> {
        val uri = intent.data ?: return finish()
        val url = uri.getQueryParameter("url") ?: return finish()

        launch {
          val uuid = withProfile {
            val type =
              when (uri.getQueryParameter("type")?.lowercase(Locale.getDefault())) {
                "url" -> Profile.Type.Url
                "file" -> Profile.Type.File
                else -> Profile.Type.Url
              }
            val name = uri.getQueryParameter("name") ?: getString(R.string.new_profile)

            create(type, name).also { patch(it, name, url, 0) }
          }
          val intent =
            mainIntent(context = this@ExternalControlActivity, action = Intents.ACTION_PROPERTIES)
              .setUUID(uuid = uuid)
          startActivity(intent)
          finish()
        }
      }

      Intents.ACTION_TOGGLE_CLASH ->
        if (Remote.broadcasts.clashRunning) {
          stopClash()
        } else {
          startClash()
        }

      Intents.ACTION_START_CLASH ->
        if (!Remote.broadcasts.clashRunning) {
          startClash()
        } else {
          toast(R.string.external_control_started)
        }

      Intents.ACTION_STOP_CLASH ->
        if (Remote.broadcasts.clashRunning) {
          stopClash()
        } else {
          toast(R.string.external_control_stopped)
        }
    }
    return finish()
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

  override fun finish() {
    super.finish()
    @Suppress("DEPRECATION") overridePendingTransition(0, 0)
  }
}
