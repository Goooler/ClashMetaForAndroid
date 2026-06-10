package com.github.kr328.clash.core.bridge

import androidx.annotation.Keep
import androidx.core.net.toUri
import com.github.kr328.clash.common.util.application
import java.io.FileNotFoundException

@Keep
object Content {
  @JvmStatic
  fun open(url: String): Int {
    val uri = url.toUri()

    if (uri.scheme != "content") {
      throw UnsupportedOperationException("Unsupported scheme ${uri.scheme}")
    }

    return application.contentResolver.openFileDescriptor(uri, "r")?.detachFd()
      ?: throw FileNotFoundException("$uri not found")
  }
}
