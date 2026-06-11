package com.github.kr328.clash.ui.util

import android.content.Context
import androidx.compose.runtime.Composable
import com.github.kr328.clash.common.Res as CommonRes
import com.github.kr328.clash.common.format_days_ago
import com.github.kr328.clash.common.format_hours_ago
import com.github.kr328.clash.common.format_minutes_ago
import com.github.kr328.clash.common.recently
import kotlin.time.Duration.Companion.milliseconds
import org.jetbrains.compose.resources.StringResource

fun Context.getString(res: Any, vararg formatArgs: Any): String {
  return when (res) {
    is Int -> getString(res, *formatArgs)
    is StringResource -> {
      val id = resources.getIdentifier(res.key, "string", packageName)
      if (id == 0) {
        throw IllegalArgumentException("Resource not found for key: ${res.key}")
      }
      getString(id, *formatArgs)
    }
    else -> throw IllegalArgumentException("Unsupported resource type: $res")
  }
}

@Composable
fun stringResCompat(res: Any, vararg formatArgs: Any): String {
  return when (res) {
    is StringResource -> org.jetbrains.compose.resources.stringResource(res, *formatArgs)
    is Int -> androidx.compose.ui.res.stringResource(res, *formatArgs)
    else -> throw IllegalArgumentException("Unsupported resource type: $res")
  }
}

fun Long.elapsedIntervalString(context: Context): String {
  val duration = this.milliseconds
  val day = duration.inWholeDays
  val hour = duration.inWholeHours
  val minute = duration.inWholeMinutes

  return when {
    day > 0 -> context.getString(CommonRes.string.format_days_ago, day)
    hour > 0 -> context.getString(CommonRes.string.format_hours_ago, hour)
    minute > 0 -> context.getString(CommonRes.string.format_minutes_ago, minute)
    else -> context.getString(CommonRes.string.recently)
  }
}
