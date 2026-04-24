package com.github.kr328.clash.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class ProxyState(val nowState: MutableState<String>) {
  constructor(now: String) : this(mutableStateOf(now))

  val now: String
    get() = nowState.value

  fun updateNow(value: String) {
    nowState.value = value
  }
}
