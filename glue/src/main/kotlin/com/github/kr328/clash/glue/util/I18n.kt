package com.github.kr328.clash.glue.util

import android.content.Context
import com.github.kr328.clash.common.compat.preferredLocale
import java.text.SimpleDateFormat
import java.util.Date

private const val DATE_DATE_ONLY = "yyyy-MM-dd"
private const val DATE_TIME_ONLY = "HH:mm:ss.SSS"
private const val DATE_ALL = "$DATE_DATE_ONLY $DATE_TIME_ONLY"

@JvmOverloads
fun Date.format(
  context: Context,
  includeDate: Boolean = true,
  includeTime: Boolean = true,
): String {
  val locale = context.resources.configuration.preferredLocale

  return when {
    includeDate && includeTime -> SimpleDateFormat(DATE_ALL, locale).format(this)
    includeDate -> SimpleDateFormat(DATE_DATE_ONLY, locale).format(this)
    includeTime -> SimpleDateFormat(DATE_TIME_ONLY, locale).format(this)
    else -> ""
  }
}

fun Long.toDateStr(): String {
  val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  return simpleDateFormat.format(Date(this))
}
