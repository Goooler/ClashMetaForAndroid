package com.github.kr328.clash.common.di

import com.github.kr328.clash.common.model.ComponentName
import kotlin.reflect.KClass
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface AppInfoProvider {
  val versionName: String
  val packageName: String
  val buildCommit: String
  val receiveBroadcastsPermission: String
  val mainActivityClass: KClass<*>
  val restartReceiverClass: KClass<*>
  val mainActivityAlias: ComponentName

  companion object : KoinComponent {
    val appInfoProvider: AppInfoProvider = get()
  }
}
