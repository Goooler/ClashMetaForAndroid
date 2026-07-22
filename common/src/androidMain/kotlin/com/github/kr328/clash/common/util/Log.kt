package com.github.kr328.clash.common.util

actual object Log {
  private const val TAG = "Tabby"

  actual fun i(message: String, throwable: Throwable?) {
    android.util.Log.i(TAG, message, throwable)
  }

  actual fun w(message: String, throwable: Throwable?) {
    android.util.Log.w(TAG, message, throwable)
  }

  actual fun e(message: String, throwable: Throwable?) {
    android.util.Log.e(TAG, message, throwable)
  }

  actual fun d(message: String, throwable: Throwable?) {
    android.util.Log.d(TAG, message, throwable)
  }

  actual fun v(message: String, throwable: Throwable?) {
    android.util.Log.v(TAG, message, throwable)
  }

  actual fun f(message: String, throwable: Throwable) {
    android.util.Log.wtf(message, throwable)
  }
}
