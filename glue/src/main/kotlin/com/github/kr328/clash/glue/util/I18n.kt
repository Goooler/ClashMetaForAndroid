package com.github.kr328.clash.glue.util

import android.content.Context
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.common.compat.preferredLocale
import com.github.kr328.clash.core.model.Provider
import com.github.kr328.clash.service.model.Profile
import java.text.SimpleDateFormat
import java.util.Date

private const val DATE_DATE_ONLY = "yyyy-MM-dd"
private const val DATE_TIME_ONLY = "HH:mm:ss.SSS"
private const val DATE_ALL = "$DATE_DATE_ONLY $DATE_TIME_ONLY"

fun Profile.Type.toString(context: Context): String {
  return when (this) {
    File -> context.getString(CommonR.string.file)
    Url -> context.getString(CommonR.string.url)
    External -> context.getString(CommonR.string.external)
  }
}

fun Provider.type(context: Context): String {
  val type =
    when (type) {
      Proxy -> context.getString(CommonR.string.proxy)
      Rule -> context.getString(CommonR.string.rule)
    }

  val vehicle =
    when (vehicleType) {
      HTTP -> context.getString(CommonR.string.http)
      File -> context.getString(CommonR.string.file)
      Inline -> context.getString(CommonR.string.inline)
      Compatible -> context.getString(CommonR.string.compatible)
    }

  return context.getString(CommonR.string.format_provider_type, type, vehicle)
}

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
