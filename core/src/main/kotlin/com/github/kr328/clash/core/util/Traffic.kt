package com.github.kr328.clash.core.util

import com.github.kr328.clash.core.model.Traffic

fun Traffic.trafficUpload(): String = trafficString(uploadScaled)

fun Traffic.trafficDownload(): String = trafficString(downloadScaled)

fun Traffic.trafficTotal(): String =
  trafficTotalString(
    normalizeScaledToCentiBytes(uploadScaled) + normalizeScaledToCentiBytes(downloadScaled)
  )

private fun normalizeScaledToCentiBytes(scaled: Long): Long {
  return if (scaled <= 1024L) scaled * 100 else scaled
}

private fun trafficTotalString(centiBytes: Long): String {
  return when {
    centiBytes >= 1024 * 1024 * 1024 * 100L -> {
      val data = centiBytes / 1024 / 1024 / 1024

      "%.2f GiB".format(data.toFloat() / 100)
    }
    centiBytes >= 1024 * 1024 * 100L -> {
      val data = centiBytes / 1024 / 1024

      "%.2f MiB".format(data.toFloat() / 100)
    }
    centiBytes >= 1024 * 100L -> {
      val data = centiBytes / 1024

      "%.2f KiB".format(data.toFloat() / 100)
    }
    else -> {
      "${centiBytes / 100} Bytes"
    }
  }
}

private fun trafficString(scaled: Long): String {
  return when {
    scaled > 1024 * 1024 * 1024 * 100L -> {
      val data = scaled / 1024 / 1024 / 1024

      "%.2f GiB".format(data.toFloat() / 100)
    }
    scaled > 1024 * 1024 * 100L -> {
      val data = scaled / 1024 / 1024

      "%.2f MiB".format(data.toFloat() / 100)
    }
    scaled > 1024 * 100L -> {
      val data = scaled / 1024

      "%.2f KiB".format(data.toFloat() / 100)
    }
    else -> {
      "$scaled Bytes"
    }
  }
}
