package com.github.kr328.clash.common.di

import kotlin.reflect.KClass
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface AppInfoProvider {
  val packageName: String
  val buildCommit: String
  val receiveBroadcastsPermission: String
  val mainActivityClass: KClass<*>
  val restartReceiverClass: KClass<*>

  companion object : KoinComponent {
    val appInfoProvider: AppInfoProvider = get()
  }
}
