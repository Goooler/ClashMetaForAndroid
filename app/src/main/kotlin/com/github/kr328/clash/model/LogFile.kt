package com.github.kr328.clash.model

import java.util.Date

data class LogFile(val fileName: String, val date: Date) {
  companion object {
    private val REGEX_FILE = "clash-(\\d+).log".toRegex()
    private const val FORMAT_FILE_NAME = "clash-%d.log"

    fun parse(fileName: String): LogFile? {
      return REGEX_FILE.matchEntire(fileName)?.run {
        LogFile(fileName, Date(groupValues[1].toLong()))
      }
    }

    fun new(): LogFile {
      val current = Date()
      val fileName = FORMAT_FILE_NAME.format(current.time)

      return LogFile(fileName, current)
    }
  }
}
