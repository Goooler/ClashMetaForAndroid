package com.github.kr328.clash.app

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.common.util.unsafeLazy
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.store.UiStore
import com.github.kr328.clash.glue.util.clashDir
import com.github.kr328.clash.glue.util.mainIntent
import com.github.kr328.clash.service.util.sendServiceRecreated
import java.io.File
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {
  private val uiStore by unsafeLazy { UiStore(this) }

  override fun attachBaseContext(base: Context?) {
    super.attachBaseContext(base)

    Global.init(this)
  }

  override fun onCreate() {
    super.onCreate()

    koin()

    val processName = getProcessName()
    extractGeoFiles()

    Log.d("Process $processName started")

    if (processName == packageName) {
      Remote.launch()
      setupShortcuts()
    } else {
      sendServiceRecreated()
    }
  }

  private fun koin() {
    startKoin {
      androidLogger()
      androidContext(this@MainApplication)
      modules(appModule)
    }
  }

  private fun setupShortcuts() {
    if (uiStore.hideAppIcon) {
      // Prevent launcher activity not found.
      ShortcutManagerCompat.removeAllDynamicShortcuts(this)
      return
    }

    val icon = IconCompat.createWithResource(this, R.mipmap.ic_launcher)
    val flags =
      Intent.FLAG_ACTIVITY_NEW_TASK or
        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
        Intent.FLAG_ACTIVITY_NO_ANIMATION

    val toggle =
      ShortcutInfoCompat.Builder(this, "toggle_clash")
        .setShortLabel(getString(R.string.shortcut_toggle_short))
        .setLongLabel(getString(R.string.shortcut_toggle_long))
        .setIcon(icon)
        .setIntent(mainIntent(action = Intents.ACTION_TOGGLE_CLASH).addFlags(flags))
        .setRank(0)
        .build()

    val start =
      ShortcutInfoCompat.Builder(this, "start_clash")
        .setShortLabel(getString(R.string.shortcut_start_short))
        .setLongLabel(getString(R.string.shortcut_start_long))
        .setIcon(icon)
        .setIntent(mainIntent(action = Intents.ACTION_START_CLASH).addFlags(flags))
        .setRank(1)
        .build()

    val stop =
      ShortcutInfoCompat.Builder(this, "stop_clash")
        .setShortLabel(getString(R.string.shortcut_stop_short))
        .setLongLabel(getString(R.string.shortcut_stop_long))
        .setIcon(icon)
        .setIntent(mainIntent(action = Intents.ACTION_STOP_CLASH).addFlags(flags))
        .setRank(2)
        .build()

    ShortcutManagerCompat.setDynamicShortcuts(this, listOf(toggle, start, stop))
  }

  private fun extractGeoFiles() {
    clashDir.mkdirs()

    val updateDate = packageManager.getPackageInfo(packageName, 0).lastUpdateTime
    val geoipFile = File(clashDir, "geoip.metadb")
    if (geoipFile.exists() && geoipFile.lastModified() < updateDate) {
      geoipFile.delete()
    }
    if (!geoipFile.exists()) {
      geoipFile.outputStream().use { assets.open("geoip.metadb").copyTo(it) }
    }

    val geositeFile = File(clashDir, "geosite.dat")
    if (geositeFile.exists() && geositeFile.lastModified() < updateDate) {
      geositeFile.delete()
    }
    if (!geositeFile.exists()) {
      geositeFile.outputStream().use { assets.open("geosite.dat").copyTo(it) }
    }

    val asnFile = File(clashDir, "ASN.mmdb")
    if (asnFile.exists() && asnFile.lastModified() < updateDate) {
      asnFile.delete()
    }
    if (!asnFile.exists()) {
      asnFile.outputStream().use { assets.open("ASN.mmdb").copyTo(it) }
    }
  }

  fun finalize() {
    Global.destroy()
  }
}
