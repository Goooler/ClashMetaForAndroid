package com.github.kr328.clash.design.component

import android.graphics.Color

class ProxyViewConfig(var proxyLine: Int) {
  var selectedControl: Int = Color.WHITE
  var selectedBackground: Int = Color.BLACK
  var unselectedControl: Int = Color.BLACK
  var unselectedBackground: Int = Color.WHITE

  fun currentUnselectedBackground(): Int =
    if (proxyLine == 1) Color.TRANSPARENT else unselectedBackground
}
