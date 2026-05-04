package com.github.kr328.clash.log.util

import android.content.Context
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.log.model.LogFile
import com.github.kr328.clash.util.logsDir
import java.io.BufferedWriter

internal class LogcatWriter(
  context: Context,
  file: LogFile = LogFile.new(),
  private val writer: BufferedWriter = context.logsDir.resolve(file.fileName).bufferedWriter(),
) : AutoCloseable by writer {

  fun appendMessage(message: LogMessage) =
    writer.appendLine(FORMAT.format(message.time.time, message.level.name, message.message))
}

private const val FORMAT = "%d:%s:%s"
