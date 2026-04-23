package com.github.kr328.clash.crash.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.log.util.SystemLogcat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppCrashedViewModel(app: Application) : AndroidViewModel(app) {
  private val _logs = MutableStateFlow("")
  val logs: StateFlow<String> = _logs

  fun loadLogs() {
    viewModelScope.launch {
      try {
        val packageInfo =
          withContext(Dispatchers.IO) {
            @Suppress("DEPRECATION")
            application.packageManager.getPackageInfo(application.packageName, 0)
          }

        Log.i(
          "App version: versionName = ${packageInfo.versionName} versionCode = ${packageInfo.longVersionCode}"
        )

        val crashLogs = withContext(Dispatchers.IO) { SystemLogcat.dumpCrash() }
        _logs.value = crashLogs
      } catch (e: Exception) {
        Log.e("Failed to load crash logs", e)
        _logs.value = "Failed to load crash logs: ${e.message}"
      }
    }
  }
}
