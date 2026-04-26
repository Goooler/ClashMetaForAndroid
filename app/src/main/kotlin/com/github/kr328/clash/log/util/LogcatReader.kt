package com.github.kr328.clash.log.util

import android.content.Context
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.model.LogFile
import com.github.kr328.clash.util.logsDir
import java.io.BufferedReader
import java.io.FileReader
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LogcatReader(
  context: Context,
  file: LogFile,
  private val reader: BufferedReader =
    BufferedReader(FileReader(context.logsDir.resolve(file.fileName))),
) : AutoCloseable by reader {

  suspend fun readAll(): List<LogMessage> =
    withContext(Dispatchers.IO) {
      var lastTime = Date(0)
      reader
        .lineSequence()
        .map { it.trim() }
        .filter { !it.startsWith("#") }
        .map { it.split(":", limit = 3) }
        .map {
          val time = it[0].toLongOrNull()?.let { date -> Date(date) } ?: lastTime
          val logMessage =
            if (it[0].toLongOrNull() != null) {
              LogMessage(time = time, level = LogMessage.Level.valueOf(it[1]), message = it[2])
            } else {
              LogMessage(
                time = time,
                level = LogMessage.Level.Warning, // or any default level
                message = it.joinToString(":"),
              )
            }
          lastTime = time
          logMessage
        }
        .toList()
    }
}
