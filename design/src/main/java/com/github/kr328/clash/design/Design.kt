package com.github.kr328.clash.design

import android.content.Context
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.github.kr328.clash.common.store.unsafeLazy
import com.github.kr328.clash.design.ui.ToastDuration
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel

sealed class Design<R>(val context: Context) :
  CoroutineScope by CoroutineScope(Dispatchers.Unconfined) {
  abstract val root: View

  val requests: Channel<R> = Channel(Channel.UNLIMITED)

  protected fun composeView(content: @Composable () -> Unit): Lazy<ComposeView> = unsafeLazy {
    ComposeView(context = context).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent(content = content)
    }
  }

  fun showToast(resId: Int, duration: ToastDuration, configure: Snackbar.() -> Unit = {}) {
    return showToast(context.getString(resId), duration, configure)
  }

  fun showToast(
    message: CharSequence,
    duration: ToastDuration,
    configure: Snackbar.() -> Unit = {},
  ) {
    root.post {
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
