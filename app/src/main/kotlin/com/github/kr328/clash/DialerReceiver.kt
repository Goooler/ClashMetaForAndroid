package com.github.kr328.clash

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.kr328.clash.MainActivity.Companion.intent as mainIntent

class DialerReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val intent = mainIntent(context = context).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
  }
}
