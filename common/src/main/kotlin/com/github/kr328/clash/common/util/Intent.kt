package com.github.kr328.clash.common.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.github.kr328.clash.common.compat.queryIntentActivitiesCompat
import com.github.kr328.clash.common.di.AppInfoProvider.Companion.instance as appInfoProvider
import java.io.Serializable
import kotlin.uuid.Uuid

fun Context.mainIntent(block: Intent.() -> Unit = {}): Intent {
  val mainActivityClass = appInfoProvider.mainActivityClass
  return Intent(this, mainActivityClass).apply(block = block)
}

val Context.mainActivityAlias: ComponentName
  get() {
    val mainActivityName = appInfoProvider.mainActivityClass.name
    val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val resolveFlags = PackageManager.MATCH_DISABLED_COMPONENTS

    return packageManager
      .queryIntentActivitiesCompat(launcherIntent, resolveFlags)
      .firstNotNullOfOrNull { resolveInfo ->
        val activityInfo = resolveInfo.activityInfo
        if (activityInfo.targetActivity == mainActivityName) {
          ComponentName(activityInfo.packageName, activityInfo.name)
        } else {
          null
        }
      }
      ?: error("Launcher alias targeting $mainActivityName is not declared in AndroidManifest.xml")
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
