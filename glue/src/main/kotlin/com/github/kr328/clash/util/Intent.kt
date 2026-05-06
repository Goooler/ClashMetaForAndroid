package com.github.kr328.clash.util

import android.content.Context
import android.content.Intent
import com.github.kr328.clash.MainActivityClassProvider
import org.koin.mp.KoinPlatform

fun mainIntent(context: Context, action: String? = null) =
  Intent(context, KoinPlatform.getKoin().get<MainActivityClassProvider>().mainActivityClass)
    .setAction(action)
