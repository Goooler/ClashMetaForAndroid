package com.github.kr328.clash.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_LONG) {
  toast(getString(resId), duration)
}

fun Context.toast(text: CharSequence, duration: Int = Toast.LENGTH_LONG) {
  Toast.makeText(this, text, duration).show()
}
