package com.github.kr328.clash.log

import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.common.util.setFileName
import com.github.kr328.clash.log.ui.LogsDesign
import com.github.kr328.clash.model.LogFile
import com.github.kr328.clash.ui.DesignActivity
import com.github.kr328.clash.util.logsDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext

class LogsActivity : DesignActivity<LogsDesign>() {

  override suspend fun main() {
    val design = LogsDesign(this)

    setContentDesign(design)

    while (isActive) {
      select {
        events.onReceive {
          when (it) {
            Event.ActivityStart -> {
              val files = withContext(Dispatchers.IO) { loadFiles() }

              design.patchLogs(files)
            }
            else -> Unit
          }
        }
        design.requests.onReceive {
          when (it) {
            LogsDesign.Request.StartLogcat -> {
              startActivity(LogcatActivity::class.intent)
              finish()
            }
            LogsDesign.Request.DeleteAll -> {
              withContext(Dispatchers.IO) { deleteAllLogs() }

              events.trySend(Event.ActivityStart)
            }
            is LogsDesign.Request.OpenFile -> {
              startActivity(LogcatActivity::class.intent.setFileName(it.file.fileName))
            }
          }
        }
      }
    }
  }

  private fun loadFiles(): List<LogFile> {
    val list = cacheDir.resolve("logs").listFiles()?.toList() ?: emptyList()

    return list.mapNotNull { LogFile.parseFromFileName(it.name) }
  }

  private fun deleteAllLogs() {
    logsDir.deleteRecursively()
  }
}
