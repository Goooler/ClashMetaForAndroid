package com.github.kr328.clash.util

import android.content.Context
import android.content.Intent
import com.github.kr328.clash.MainActivityClassProvider

fun mainIntent(context: Context, action: String? = null) =
  Intent(context, (context.applicationContext as MainActivityClassProvider).mainActivityClass)
    .setAction(action)
