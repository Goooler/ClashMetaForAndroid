package com.github.kr328.clash.common.compat

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Handler
import androidx.annotation.ColorRes
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat

fun Context.getColorCompat(@ColorRes id: Int): Int = ContextCompat.getColor(this, id)

fun Context.registerReceiverCompat(
  receiver: BroadcastReceiver,
  filter: IntentFilter,
  permission: String? = null,
  scheduler: Handler? = null,
) =
  ContextCompat.registerReceiver(
    this,
    receiver,
    filter,
    permission,
    scheduler,
    if (permission == null) ContextCompat.RECEIVER_EXPORTED else ContextCompat.RECEIVER_NOT_EXPORTED,
  )

fun Service.startForegroundCompat(id: Int, notification: Notification) =
  ServiceCompat.startForeground(
    this,
    id,
    notification,
    @Suppress("InlinedApi") ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
  )
