package com.github.kr328.clash.common.util

actual object Log {
  private const val TAG = "Tabby"

  actual fun i(message: String, throwable: Throwable?) {}

  actual fun w(message: String, throwable: Throwable?) {}

  actual fun e(message: String, throwable: Throwable?) {}

  actual fun d(message: String, throwable: Throwable?) {}

  actual fun v(message: String, throwable: Throwable?) {}

  actual fun f(message: String, throwable: Throwable) {}
}
