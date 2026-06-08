package com.github.kr328.clash.glue.util

import android.content.Context
import android.content.Intent
import android.net.VpnService
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.glue.store.UiStore
import com.github.kr328.clash.service.ClashService
import com.github.kr328.clash.service.TunService
import com.github.kr328.clash.service.util.sendBroadcastSelf

fun Context.startClashService(): Intent? {
  val startTun = UiStore.instance.enableVpn

  if (startTun) {
    val vpnRequest = VpnService.prepare(this)
    if (vpnRequest != null) return vpnRequest

    startForegroundService(TunService::class.intent)
  } else {
    startForegroundService(ClashService::class.intent)
  }

  return null
}

fun Context.stopClashService() {
  sendBroadcastSelf(Intent(Intents.ACTION_CLASH_REQUEST_STOP))
}
