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
  private val reader: BufferedReader = context.logsDir.resolve(file.fileName).bufferedReader(),
) : AutoCloseable by reader {

  suspend fun readAll(): List<LogMessage> =
    withContext(Dispatchers.IO) {
      var lastTime = Date(0)
      reader
        .lineSequence()
        .map { it.trim() }
        .filter { !it.startsWith("#") }
        .map { line ->
          val parts = line.split(":", limit = 3)
          val parsedTime = parts[0].toLongOrNull()
          val time = parsedTime?.let { date -> Date(date) } ?: lastTime
          val logMessage =
            if (parsedTime != null && parts.size >= 3) {
              LogMessage(
                time = time,
                level = LogMessage.Level.valueOf(parts[1]),
                message = parts[2],
              )
            } else {
              LogMessage(time = time, level = LogMessage.Level.Warning, message = line)
            }
          lastTime = time
          logMessage
        }
        .toList()
    }
}
