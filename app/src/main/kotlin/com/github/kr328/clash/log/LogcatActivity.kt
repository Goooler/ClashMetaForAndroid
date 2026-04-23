package com.github.kr328.clash.log

import android.content.ComponentName
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.github.kr328.clash.R
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.common.util.fileName
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.common.util.ticker
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.log.ui.LogcatDesign
import com.github.kr328.clash.log.util.LogcatFilter
import com.github.kr328.clash.log.util.LogcatReader
import com.github.kr328.clash.model.LogFile
import com.github.kr328.clash.ui.BaseActivity
import com.github.kr328.clash.ui.ToastDuration
import com.github.kr328.clash.util.logsDir
import com.github.kr328.clash.util.showExceptionToast
import java.io.OutputStreamWriter
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class LogcatActivity : BaseActivity<LogcatDesign>() {
  private var conn: ServiceConnection? = null

  override suspend fun main() {
    val fileName = intent?.fileName

    if (fileName != null) {
      val file = LogFile.parseFromFileName(fileName) ?: return showInvalid()

      return mainLocalFile(file)
    }

    return mainStreaming()
  }

  private suspend fun mainLocalFile(file: LogFile) {
    val messages =
      try {
        LogcatReader(this, file).readAll()
      } catch (e: Exception) {
        Log.e("Fail to read log file ${file.fileName}: ${e.message}")
        return showInvalid()
      }

    val design = LogcatDesign(this, false)

    setContentDesign(design)

    design.patchMessages(messages)

    while (isActive) {
      when (design.requests.receive()) {
        LogcatDesign.Request.Delete -> {
          withContext(Dispatchers.IO) { logsDir.resolve(file.fileName).delete() }

          finish()
        }
        LogcatDesign.Request.Export -> {
          val output =
            startActivityForResult(
              ActivityResultContracts.CreateDocument("text/plain"),
              file.fileName,
            )

          if (output != null) {
            try {
              writeLogTo(design, messages, file, output)

              design.showToast(R.string.file_exported, ToastDuration.Long)
            } catch (e: Exception) {
              design.showExceptionToast(e)
            }
          }
        }
        else -> Unit
      }
    }
  }

  private suspend fun mainStreaming() {
    val design = LogcatDesign(this, true)

    setContentDesign(design)

    startForegroundService(LogcatService::class.intent)

    val logcat = bindLogcatService()
    val ticker = ticker(500)

    var initial = true

    while (isActive) {
      select {
        events.onReceive {}

        design.requests.onReceive {
          when (it) {
            LogcatDesign.Request.Close -> {
              stopService(LogcatService::class.intent)
              startActivity(LogsActivity::class.intent)
              finish()
            }
            else -> Unit
          }
        }
        if (activityStarted) {
          ticker.onReceive {
            val snapshot = logcat.snapshot(initial) ?: return@onReceive

            design.patchMessages(snapshot.messages)

            initial = false
          }
        }
      }
    }
  }

  override fun onDestroy() {
    conn?.apply(this::unbindService)

    super.onDestroy()
  }

  private suspend fun bindLogcatService(): LogcatService {
    return suspendCancellableCoroutine { ctx ->
      bindService(
        LogcatService::class.intent,
        object : ServiceConnection {
          override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val srv = service!!.queryLocalInterface("") as LogcatService

            ctx.resume(srv)

            conn = this
          }

          override fun onServiceDisconnected(name: ComponentName?) {
            conn = null
          }
        },
        BIND_AUTO_CREATE,
      )
    }
  }

  private suspend fun writeLogTo(
    design: LogcatDesign,
    messages: List<LogMessage>,
    file: LogFile,
    uri: Uri,
  ) =
    withContext(Dispatchers.IO) {
      LogcatFilter(OutputStreamWriter(contentResolver.openOutputStream(uri)), this@LogcatActivity)
        .use {
          design.startExportProgress(messages.size)

          try {
            it.writeHeader(file.date)

            messages.forEachIndexed { idx, msg ->
              design.updateExportProgress(idx + 1)

              it.writeMessage(msg)
            }
          } finally {
            design.finishExportProgress()
          }
        }
    }

  private fun showInvalid() {
    Toast.makeText(this, R.string.invalid_log_file, Toast.LENGTH_LONG).show()
  }
}
