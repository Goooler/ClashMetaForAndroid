package com.github.kr328.clash.app

import android.app.Application
import android.content.Context
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.util.clashDir
import com.github.kr328.clash.home.homeModule
import com.github.kr328.clash.service.util.sendServiceRecreated
import java.io.File
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {

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
    } else {
      sendServiceRecreated()
    }
  }

  private fun koin() {
    startKoin {
      androidLogger()
      androidContext(this@MainApplication)
      modules(appModule, homeModule)
    }
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
