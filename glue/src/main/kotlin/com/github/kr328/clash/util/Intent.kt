package com.github.kr328.clash.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.github.kr328.clash.di.AppInfoProvider.Companion.instance as appInfoProvider

fun Context.mainIntent(action: String? = null): Intent {
  val mainActivityClass = appInfoProvider.mainActivityClass
  return Intent(this, mainActivityClass).setAction(action)
}

val Context.mainActivityAlias: ComponentName
  get() {
    val mainActivityName = appInfoProvider.mainActivityClass.name
    val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val resolveFlags = PackageManager.MATCH_DISABLED_COMPONENTS

    return packageManager
      .queryIntentActivities(launcherIntent, resolveFlags)
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
