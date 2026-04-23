package com.github.kr328.clash.ui

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.compose.runtime.Composable
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.channels.Channel

abstract class Design<R>(val context: Context) {

  val requests: Channel<R> = Channel(Channel.UNLIMITED)

  @Composable abstract fun Content()

  fun showToast(resId: Int, duration: ToastDuration, configure: Snackbar.() -> Unit = {}) {
    return showToast(context.getString(resId), duration, configure)
  }

  fun showToast(
    message: CharSequence,
    duration: ToastDuration,
    configure: Snackbar.() -> Unit = {},
  ) {
    (context as Activity).findViewById<View>(android.R.id.content)?.let { root ->
      Snackbar.make(
          root,
          message,
          when (duration) {
            ToastDuration.Short -> Snackbar.LENGTH_SHORT
            ToastDuration.Long -> Snackbar.LENGTH_LONG
            ToastDuration.Indefinite -> Snackbar.LENGTH_INDEFINITE
          },
        )
        .apply(configure)
        .show()
    }
  }
}
