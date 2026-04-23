package com.github.kr328.clash.crash.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.log.util.dumpCrash
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppCrashedViewModel(app: Application) : AndroidViewModel(app) {
  val logs: StateFlow<String>
    field = MutableStateFlow("")

  fun loadLogs() {
    viewModelScope.launch {
      runCatching {
          val packageInfo = application.packageManager.getPackageInfo(application.packageName, 0)
          Log.i(
            "App version: versionName = ${packageInfo.versionName} versionCode = ${packageInfo.longVersionCode}"
          )
          logs.value = dumpCrash()
        }
        .getOrElse { e ->
          Log.e("Failed to load crash logs", e)
          logs.value = "Failed to load crash logs: ${e.stackTraceToString()}"
        }
    }
  }
}
