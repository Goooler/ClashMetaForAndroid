package com.github.kr328.clash.common.util

expect object Log {
  fun i(message: String, throwable: Throwable? = null)

  fun w(message: String, throwable: Throwable? = null)

  fun e(message: String, throwable: Throwable? = null)

  fun d(message: String, throwable: Throwable? = null)

  fun v(message: String, throwable: Throwable? = null)

  fun f(message: String, throwable: Throwable)
}
