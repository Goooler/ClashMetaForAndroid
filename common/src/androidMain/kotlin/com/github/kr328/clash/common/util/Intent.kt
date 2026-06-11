package com.github.kr328.clash.common.util

import android.content.Intent
import android.net.Uri
import android.os.Build
import com.github.kr328.clash.common.di.AppInfoProvider.Companion.appInfoProvider
import java.io.Serializable
import kotlin.uuid.Uuid

fun mainIntent(block: Intent.() -> Unit = {}): Intent {
  return appInfoProvider.mainActivityClass.intent.apply(block = block)
}

fun Intent.grantPermissions(read: Boolean = true, write: Boolean = true): Intent = apply {
  var flags = 0
  if (read) flags = flags or Intent.FLAG_GRANT_READ_URI_PERMISSION
  if (write) flags = flags or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
  addFlags(flags)
}

var Intent.uuid: Uuid?
  get() = data?.takeIf { it.scheme == "uuid" }?.schemeSpecificPart?.let(Uuid::parse)
  set(value) {
    data =
      if (value == null) {
        null
      } else {
        Uri.fromParts("uuid", value.toString(), null)
      }
  }

fun Intent.setUUID(uuid: Uuid): Intent = apply { this.uuid = uuid }

inline fun <reified T : Serializable> Intent.getSerializableCompat(key: String): T? =
  when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
      getSerializableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
  }
