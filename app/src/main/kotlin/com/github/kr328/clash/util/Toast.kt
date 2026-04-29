package com.github.kr328.clash.util

import android.app.Activity
import android.widget.Toast
import androidx.annotation.StringRes

fun Activity.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_LONG) {
  Toast.makeText(this, resId, duration).show()
}
