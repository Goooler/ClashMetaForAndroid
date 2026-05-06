package com.github.kr328.clash.util

import android.content.Context
import android.content.Intent
import com.github.kr328.clash.mainActivityClassProvider

fun mainIntent(context: Context, action: String? = null) =
  Intent(context, mainActivityClassProvider.mainActivityClass).setAction(action)
