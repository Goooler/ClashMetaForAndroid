package com.github.kr328.clash.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.kr328.clash.glue.util.startClashService
import com.github.kr328.clash.service.StatusProvider

class RestartReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    when (intent.action) {
      Intent.ACTION_BOOT_COMPLETED,
      Intent.ACTION_MY_PACKAGE_REPLACED -> {
        if (StatusProvider.shouldStartClashOnBoot) context.startClashService()
      }
    }
  }
}
