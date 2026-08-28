package com.github.kr328.clash.glue.util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import co.touchlab.kermit.Logger
import java.io.File
import java.util.zip.ZipFile

object ApplicationObserver {
  private val _visibleActivities: MutableSet<Activity> = mutableSetOf()

  private var visibleChanged: (Boolean) -> Unit = {}

  private var appVisible = false
    set(value) {
      if (field != value) {
        field = value

        visibleChanged(value)
      }
    }

  val createdActivities: Set<Activity>
    field: MutableSet<Activity> = mutableSetOf()

  private val activityObserver =
    object : Application.ActivityLifecycleCallbacks {
      @Synchronized
      override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        createdActivities.add(activity)
      }

      @Synchronized
      override fun onActivityDestroyed(activity: Activity) {
        createdActivities.remove(activity)
        _visibleActivities.remove(activity)
        appVisible = _visibleActivities.isNotEmpty()
      }

      override fun onActivityStarted(activity: Activity) {
        _visibleActivities.add(activity)
        appVisible = true
      }

      override fun onActivityStopped(activity: Activity) {
        _visibleActivities.remove(activity)
        appVisible = _visibleActivities.isNotEmpty()
      }

      override fun onActivityPaused(activity: Activity) {}

      override fun onActivityResumed(activity: Activity) {}

      override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    }

  fun onVisibleChanged(visibleChanged: (Boolean) -> Unit) {
    this.visibleChanged = visibleChanged
  }

  fun attach(application: Application) {
    application.registerActivityLifecycleCallbacks(activityObserver)
  }
}

fun Context.verifyApk(): Boolean {
  return try {
    val info = applicationInfo
    val sources = info.splitSourceDirs ?: arrayOf(info.sourceDir) ?: return false

    val regexNativeLibrary = "lib/(\\S+)/libclash.so".toRegex()
    val availableAbi = Build.SUPPORTED_ABIS.toSet()
    val apkAbi =
      sources
        .asSequence()
        .filter { File(it).exists() }
        .flatMap { ZipFile(it).entries().asSequence() }
        .mapNotNull { regexNativeLibrary.matchEntire(it.name) }
        .mapNotNull { it.groups[1]?.value }
        .toSet()

    availableAbi.intersect(apkAbi).isNotEmpty()
  } catch (e: Exception) {
    Logger.e("Verify apk failed: ${e.message}", e)
    false
  }
}
