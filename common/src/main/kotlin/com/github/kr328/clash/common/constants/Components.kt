package com.github.kr328.clash.common.constants

import android.content.ComponentName
import com.github.kr328.clash.common.packageName

object Components {
  private const val PKG_NAME = "com.github.kr328.clash"

  val MAIN_ACTIVITY = ComponentName(packageName, "$PKG_NAME.MainActivity")
}
