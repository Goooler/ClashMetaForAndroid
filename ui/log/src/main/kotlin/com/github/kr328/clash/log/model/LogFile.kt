package com.github.kr328.clash.log.model

data class LogFile(val fileName: String, val created: Long) {
  companion object {
    private val REGEX_FILE = "clash-(\\d+).log".toRegex()
    private const val FORMAT_FILE_NAME = "clash-%d.log"

    fun parse(fileName: String): LogFile? {
      return REGEX_FILE.matchEntire(fileName)?.run { LogFile(fileName, groupValues[1].toLong()) }
    }

    fun new(): LogFile {
      val current = System.currentTimeMillis()
      val fileName = FORMAT_FILE_NAME.format(current)

      return LogFile(fileName, current)
    }
  }
}
