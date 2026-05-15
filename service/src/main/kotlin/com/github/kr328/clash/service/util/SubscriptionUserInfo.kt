package com.github.kr328.clash.service.util

import android.content.Context
import java.math.BigDecimal
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
    Request.Builder().url(source).header("User-Agent", "ClashMetaForAndroid/$versionName").build()

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
    val info = flag.split("=")
    when {
      info[0].contains("upload") && info[1].isNotEmpty() ->
        upload = BigDecimal(info[1].split('.').first()).longValueExact()

      info[0].contains("download") && info[1].isNotEmpty() ->
        download = BigDecimal(info[1].split('.').first()).longValueExact()

      info[0].contains("total") && info[1].isNotEmpty() ->
        total = BigDecimal(info[1].split('.').first()).longValueExact()

      info[0].contains("expire") && info[1].isNotEmpty() ->
        expire = (info[1].toDouble() * 1000).toLong()
    }
  }

  return SubscriptionUserInfo(upload = upload, download = download, total = total, expire = expire)
}
