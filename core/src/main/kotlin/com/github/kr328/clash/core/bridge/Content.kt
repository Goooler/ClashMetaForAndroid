package com.github.kr328.clash.core.bridge

import android.app.Application
import androidx.annotation.Keep
import androidx.core.net.toUri
import java.io.FileNotFoundException
import org.koin.core.context.GlobalContext

@Keep
object Content {
  @JvmStatic
  fun open(url: String): Int {
    val uri = url.toUri()

    if (uri.scheme != "content") {
      throw UnsupportedOperationException("Unsupported scheme ${uri.scheme}")
    }

    return GlobalContext.get()
      .get<Application>()
      .contentResolver
      .openFileDescriptor(uri, "r")
      ?.detachFd() ?: throw FileNotFoundException("$uri not found")
  }
}
