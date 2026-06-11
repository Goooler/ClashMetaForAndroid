package com.github.kr328.clash.profile.util

import androidx.compose.runtime.Composable
import com.github.kr328.clash.common.Res as CommonRes
import com.github.kr328.clash.common.format_days_ago
import com.github.kr328.clash.common.format_hours_ago
import com.github.kr328.clash.common.format_minutes_ago
import com.github.kr328.clash.common.recently
import kotlin.time.Duration.Companion.milliseconds
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun Long.elapsedIntervalString(): String {
  val duration = this.milliseconds
  val day = duration.inWholeDays
  val hour = duration.inWholeHours
  val minute = duration.inWholeMinutes

  return when {
    day > 0 -> stringResource(CommonRes.string.format_days_ago, day)
    hour > 0 -> stringResource(CommonRes.string.format_hours_ago, hour)
    minute > 0 -> stringResource(CommonRes.string.format_minutes_ago, minute)
    else -> stringResource(CommonRes.string.recently)
  }
}
