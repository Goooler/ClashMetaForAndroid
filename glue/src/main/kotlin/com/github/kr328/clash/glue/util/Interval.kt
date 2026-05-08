package com.github.kr328.clash.glue.util

import android.content.Context
import com.github.kr328.clash.common.R as CommonR
import kotlin.time.Duration.Companion.milliseconds

fun Long.elapsedIntervalString(context: Context): String {
  val duration = this.milliseconds
  val day = duration.inWholeDays
  val hour = duration.inWholeHours
  val minute = duration.inWholeMinutes

  return when {
    day > 0 -> context.getString(CommonR.string.format_days_ago, day)
    hour > 0 -> context.getString(CommonR.string.format_hours_ago, hour)
    minute > 0 -> context.getString(CommonR.string.format_minutes_ago, minute)
    else -> context.getString(CommonR.string.recently)
  }
}
