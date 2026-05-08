package com.github.kr328.clash.glue.di

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface AppInfoProvider {
  val mainActivityClass: Class<*>
  val restartReceiverClass: Class<*>

  companion object : KoinComponent {
    val instance: AppInfoProvider by inject(mode = NONE)
  }
}
