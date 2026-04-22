package com.github.kr328.clash.design.component

import android.content.Context
import android.graphics.Color
import com.github.kr328.clash.design.util.resolveThemedColor

class ProxyViewConfig(val context: Context, var proxyLine: Int) {
  private val colorSurface =
    context.resolveThemedColor(com.google.android.material.R.attr.colorSurface)

  val selectedControl =
    context.resolveThemedColor(com.google.android.material.R.attr.colorOnPrimary)
  val selectedBackground = context.resolveThemedColor(android.R.attr.colorPrimary)

  val unselectedControl =
    context.resolveThemedColor(com.google.android.material.R.attr.colorOnSurface)
  val unselectedBackground: Int
    get() = if (proxyLine == 1) Color.TRANSPARENT else colorSurface
}
