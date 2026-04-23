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

  fun snackbar(resId: Int, duration: SnackbarDuration, configure: Snackbar.() -> Unit = {}) {
    return snackbar(context.getString(resId), duration, configure)
  }

  fun snackbar(
    message: CharSequence,
    duration: SnackbarDuration,
    configure: Snackbar.() -> Unit = {},
  ) {
    (context as Activity).findViewById<View>(android.R.id.content)?.let { root ->
      Snackbar.make(
          root,
          message,
          when (duration) {
            SnackbarDuration.Short -> Snackbar.LENGTH_SHORT
            SnackbarDuration.Long -> Snackbar.LENGTH_LONG
            SnackbarDuration.Indefinite -> Snackbar.LENGTH_INDEFINITE
          },
        )
        .apply(configure)
        .show()
    }
  }
}
