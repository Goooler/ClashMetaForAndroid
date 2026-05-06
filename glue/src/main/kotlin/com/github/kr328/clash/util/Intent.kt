package com.github.kr328.clash.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import org.koin.mp.KoinPlatform

interface MainActivityClassProvider {
  val mainActivityClass: Class<out Activity>
}

fun mainIntent(context: Context, action: String? = null): Intent {
  val mainActivityClass = KoinPlatform.getKoin().get<MainActivityClassProvider>().mainActivityClass
  return Intent(context, mainActivityClass).setAction(action)
}
