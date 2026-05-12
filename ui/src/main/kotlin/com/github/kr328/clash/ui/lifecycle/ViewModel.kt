package com.github.kr328.clash.ui.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.defaultViewModelCreationExtras
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
inline fun <reified VM> viewModelWithLifecycle(
  viewModelStoreOwner: ViewModelStoreOwner =
    checkNotNull(LocalViewModelStoreOwner.current) {
      "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
  key: String? = null,
  factory: ViewModelProvider.Factory? = null,
  extras: CreationExtras = viewModelStoreOwner.defaultViewModelCreationExtras,
  lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
): VM where VM : ViewModel, VM : LifecycleObserver {
  val vm =
    viewModel<VM>(
      viewModelStoreOwner = viewModelStoreOwner,
      key = key,
      factory = factory,
      extras = extras,
    )
  DisposableEffect(lifecycleOwner, vm) {
    lifecycleOwner.lifecycle.addObserver(vm)
    onDispose { lifecycleOwner.lifecycle.removeObserver(vm) }
  }
  return vm
}
