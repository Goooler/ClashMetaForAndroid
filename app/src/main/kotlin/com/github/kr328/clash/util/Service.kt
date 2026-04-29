package com.github.kr328.clash.util

import android.content.Context
import android.content.ServiceConnection

fun Context.unbindServiceSilent(connection: ServiceConnection) {
  runCatching { unbindService(connection) }
}
