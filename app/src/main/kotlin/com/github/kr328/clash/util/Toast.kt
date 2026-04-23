package com.github.kr328.clash.util

import com.github.kr328.clash.R
import com.github.kr328.clash.ui.Design
import com.github.kr328.clash.ui.ToastDuration
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Design<*>.showExceptionSnackbar(message: CharSequence) {
  showToast(message, ToastDuration.Long) {
    setAction(R.string.detail) {
      MaterialAlertDialogBuilder(it.context)
        .setTitle(R.string.error)
        .setMessage(message)
        .setCancelable(true)
        .setPositiveButton(R.string.ok) { _, _ -> }
        .show()
    }
  }
}

fun Design<*>.showExceptionSnackbar(exception: Exception) {
  showExceptionSnackbar(exception.message ?: "Unknown")
}
