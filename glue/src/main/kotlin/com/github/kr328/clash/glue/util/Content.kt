package com.github.kr328.clash.glue.util

import android.content.ContentResolver
import android.net.Uri
import java.io.FileNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun ContentResolver.copyContentTo(source: Uri, target: Uri) {
  withContext(Dispatchers.IO) {
    (openInputStream(source) ?: fileNotFound(source)).use { input ->
      (openOutputStream(target, "rwt") ?: fileNotFound(target)).use { output ->
        input.copyTo(output)
      }
    }
  }
}

private fun fileNotFound(file: Uri): Nothing {
  throw FileNotFoundException("$file not found")
}
