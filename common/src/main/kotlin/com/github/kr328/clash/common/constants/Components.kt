package com.github.kr328.clash.common.constants

import android.content.ComponentName
import com.github.kr328.clash.common.util.packageName

object Components {
  private const val PKG_NAME = "com.github.kr328.clash"

  val MAIN_ACTIVITY = ComponentName(packageName, "$PKG_NAME.MainActivity")
  val PROPERTIES_ACTIVITY = ComponentName(packageName, "$PKG_NAME.PropertiesActivity")
}
