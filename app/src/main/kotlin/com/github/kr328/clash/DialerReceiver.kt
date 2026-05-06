package com.github.kr328.clash

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.kr328.clash.util.mainIntent

class DialerReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val intent = context.mainIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
  }
}
