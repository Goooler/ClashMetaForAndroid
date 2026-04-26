package com.github.kr328.clash.common.util

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> unsafeLazy(noinline initializer: () -> T): Lazy<T> =
  lazy(LazyThreadSafetyMode.NONE, initializer)
