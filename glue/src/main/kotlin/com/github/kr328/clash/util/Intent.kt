package com.github.kr328.clash.util

import android.content.Context
import android.content.Intent
import org.koin.mp.KoinPlatform

interface MainActivityClassProvider {
  val mainActivityClass: Class<*>
}

private val mainActivityClass =
  KoinPlatform.getKoin().get<MainActivityClassProvider>().mainActivityClass

fun mainIntent(context: Context, action: String? = null) =
  Intent(context, mainActivityClass).setAction(action)
