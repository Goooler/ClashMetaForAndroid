package com.github.kr328.clash.util

import android.content.Context
import android.content.Intent

fun mainIntent(context: Context, action: String? = null) =
  Intent(context, Class.forName("com.github.kr328.clash.MainActivity")).setAction(action)
