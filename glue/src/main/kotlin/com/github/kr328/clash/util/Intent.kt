package com.github.kr328.clash.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.github.kr328.clash.di.AppInfoProvider.Companion.instance as appInfoProvider

fun mainIntent(context: Context, action: String? = null): Intent {
  val mainActivityClass = appInfoProvider.mainActivityClass
  return Intent(context, mainActivityClass).setAction(action)
}

val Context.mainActivityAlias: ComponentName
  get() = ComponentName(this, appInfoProvider.mainActivityAlias)
