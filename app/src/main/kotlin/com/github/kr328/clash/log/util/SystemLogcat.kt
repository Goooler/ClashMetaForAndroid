package com.github.kr328.clash.log.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val command =
  arrayOf("logcat", "-d", "-s", "Go", "DEBUG", "AndroidRuntime", "ClashMetaForAndroid", "LwIP")

suspend fun dumpCrash(): String =
  withContext(Dispatchers.IO) {
    runCatching {
        val process = Runtime.getRuntime().exec(command)
        val result =
          process.inputStream.use { stream ->
            stream.reader().readLines().filterNot { it.startsWith("------") }.joinToString("\n")
          }
        process.waitFor()
        result.trim()
      }
      .getOrNull()
      .orEmpty()
  }
