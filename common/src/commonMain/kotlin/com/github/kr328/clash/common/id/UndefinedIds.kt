package com.github.kr328.clash.common.id

import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.updateAndFetch

@OptIn(ExperimentalAtomicApi::class)
object UndefinedIds {
  private const val PREFIX = 0x14000000
  private const val MASK = 0x00FFFFFF

  private val current = AtomicInt(0)

  fun next(): Int {
    return current.updateAndFetch { ((it and MASK) + 1) or PREFIX }
  }
}
