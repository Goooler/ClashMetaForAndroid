package com.github.kr328.clash.di

import android.app.Activity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface AppInfoProvider {
  val mainActivityClass: Class<out Activity>
  val mainActivityAlias: String

  companion object : KoinComponent {
    val instance: AppInfoProvider by inject()
  }
}
