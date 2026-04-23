package com.github.kr328.clash.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import com.github.kr328.clash.R
import com.github.kr328.clash.ui.Design
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Design<*>.showExceptionSnackbar(message: CharSequence) {
  snackbar(message) {
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
  showExceptionSnackbar(exception.message ?: context.getString(R.string.unknown))
}

fun Context.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_LONG) {
  toast(getString(resId), duration)
}

fun Context.toast(text: String, duration: Int = Toast.LENGTH_LONG) {
  Toast.makeText(this, text, duration).show()
}
