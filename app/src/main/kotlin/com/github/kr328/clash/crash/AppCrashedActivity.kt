package com.github.kr328.clash.crash

import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.crash.ui.AppCrashedDesign
import com.github.kr328.clash.log.util.SystemLogcat
import com.github.kr328.clash.ui.DesignActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

class AppCrashedActivity : DesignActivity<AppCrashedDesign>() {
  override suspend fun main() {
    val design = AppCrashedDesign(this)

    setContentDesign(design)

    val packageInfo = withContext(Dispatchers.IO) { packageManager.getPackageInfo(packageName, 0) }

    Log.i(
      "App version: versionName = ${packageInfo.versionName} versionCode = ${packageInfo.longVersionCode}"
    )

    val logs = withContext(Dispatchers.IO) { SystemLogcat.dumpCrash() }

    design.updateLogs(logs)

    while (isActive) {
      events.receive()
    }
  }
}
