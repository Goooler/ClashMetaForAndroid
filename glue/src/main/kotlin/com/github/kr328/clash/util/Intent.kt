package com.github.kr328.clash.util

import android.content.Context
import android.content.Intent

private val mainActivityClass = Class.forName("com.github.kr328.clash.MainActivity")

fun mainIntent(context: Context, action: String? = null) =
  Intent(context, mainActivityClass).setAction(action)
