package com.github.kr328.clash.log.util

import android.content.Context
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.model.LogFile
import com.github.kr328.clash.util.logsDir
import java.io.BufferedWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LogcatWriter(
  context: Context,
  file: LogFile = LogFile.new(),
  private val writer: BufferedWriter = context.logsDir.resolve(file.fileName).bufferedWriter(),
) : AutoCloseable by writer {

  suspend fun appendMessage(message: LogMessage) =
    withContext(Dispatchers.IO) {
      writer.appendLine(FORMAT.format(message.time.time, message.level.name, message.message))
    }
}

private const val FORMAT = "%d:%s:%s"
