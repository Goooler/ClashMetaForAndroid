package com.github.kr328.clash.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.common.compat.registerReceiverCompat
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.di.AppInfoProvider.Companion.appInfoProvider
import com.github.kr328.clash.glue.remote.StatusClient
import com.github.kr328.clash.glue.util.startClashService
import com.github.kr328.clash.glue.util.stopClashService

class TileService : android.service.quicksettings.TileService() {
  private var currentProfile = ""
  private var clashRunning = false

  override fun onClick() {
    val tile = qsTile ?: return

    when (tile.state) {
      Tile.STATE_INACTIVE -> {
        startClashService()
      }
      Tile.STATE_ACTIVE -> {
        stopClashService()
      }
    }
  }

  override fun onStartListening() {
    super.onStartListening()

    registerReceiverCompat(
      receiver,
      IntentFilter().apply {
        addAction(Intents.ACTION_CLASH_STARTED)
        addAction(Intents.ACTION_CLASH_STOPPED)
        addAction(Intents.ACTION_PROFILE_LOADED)
        addAction(Intents.ACTION_SERVICE_RECREATED)
      },
      appInfoProvider.receiveBroadcastsPermission,
      null,
    )

    val name = StatusClient(this).currentProfile()

    clashRunning = name != null
    currentProfile = name.orEmpty()

    updateTile()
  }

  override fun onStopListening() {
    super.onStopListening()

    unregisterReceiver(receiver)
  }

  private fun updateTile() {
    val tile = qsTile ?: return

    tile.state = if (clashRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE

    tile.label = currentProfile.ifEmpty { getText(CommonR.string.tabby) }

    tile.icon = Icon.createWithResource(this, CommonR.drawable.ic_tabby_small)

    tile.updateTile()
  }

  private val receiver =
    object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
          Intents.ACTION_CLASH_STARTED -> {
            clashRunning = true

            currentProfile = ""
          }
          Intents.ACTION_CLASH_STOPPED,
          Intents.ACTION_SERVICE_RECREATED -> {
            clashRunning = false

            currentProfile = ""
          }
          Intents.ACTION_PROFILE_LOADED -> {
            currentProfile = StatusClient(this@TileService).currentProfile().orEmpty()
          }
        }

        updateTile()
      }
    }
}
