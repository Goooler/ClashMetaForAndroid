package com.github.kr328.clash.glue.util

import android.content.Context
import android.content.ServiceConnection

fun Context.unbindServiceSilent(connection: ServiceConnection) {
  try {
    unbindService(connection)
  } catch (_: Exception) {}
}
