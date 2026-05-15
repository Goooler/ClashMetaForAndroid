package com.github.kr328.clash.service.util

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request

data class SubscriptionUserInfo(
  val upload: Long,
  val download: Long,
  val total: Long,
  val expire: Long,
)

fun Context.fetchSubscriptionUserInfo(source: String): SubscriptionUserInfo? {
  val versionName = packageManager.getPackageInfo(packageName, 0).versionName
  val request =
    Request.Builder().url(source).header("User-Agent", "Tabby/$versionName").build()

  OkHttpClient().newCall(request).execute().use { response ->
    if (!response.isSuccessful) return null
    val userinfo = response.headers["subscription-userinfo"] ?: return null
    return parseSubscriptionUserInfo(userinfo)
  }
}

private fun parseSubscriptionUserInfo(userinfo: String): SubscriptionUserInfo {
  var upload: Long = 0
  var download: Long = 0
  var total: Long = 0
  var expire: Long = 0

  val flags = userinfo.split(";")
  for (flag in flags) {
    val (key, value) = flag.split("=")
    when {
      key.contains("upload") && value.isNotEmpty() ->
        upload = value.split('.').first().toBigDecimal().longValueExact()

      key.contains("download") && value.isNotEmpty() ->
        download = value.split('.').first().toBigDecimal().longValueExact()

      key.contains("total") && value.isNotEmpty() ->
        total = value.split('.').first().toBigDecimal().longValueExact()

      key.contains("expire") && value.isNotEmpty() -> expire = (value.toDouble() * 1000L).toLong()
    }
  }

  return SubscriptionUserInfo(upload = upload, download = download, total = total, expire = expire)
}
