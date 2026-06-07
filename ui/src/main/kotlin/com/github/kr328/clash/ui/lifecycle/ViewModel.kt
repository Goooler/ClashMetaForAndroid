package com.github.kr328.clash.ui.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
inline fun <reified VM> VM.withLifecycle(
  lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
): VM where VM : ViewModel, VM : LifecycleObserver = apply {
  val vm = this
  DisposableEffect(lifecycleOwner, vm) {
    lifecycleOwner.lifecycle.addObserver(vm)
    onDispose { lifecycleOwner.lifecycle.removeObserver(vm) }
  }
}
