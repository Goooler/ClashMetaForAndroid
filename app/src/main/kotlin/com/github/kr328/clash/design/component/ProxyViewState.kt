package com.github.kr328.clash.design.component

import android.graphics.Color
import com.github.kr328.clash.core.model.Proxy
import com.github.kr328.clash.design.model.ProxyState
import kotlin.math.absoluteValue
import kotlin.math.max

class ProxyViewState(
  val proxy: Proxy,
  private val parent: ProxyState,
  private val link: ProxyState?,
) {
  var title: String = ""
  var subtitle: String = ""
  var delayText: String = ""
  var background: Int = Color.TRANSPARENT
  var controls: Int = Color.BLACK

  private var delay: Int = 0
  private var selected: Boolean = false
  private var parentNow: String = ""
  private var linkNow: String? = null

  private var lastFrameTime = System.currentTimeMillis()

  fun update(
    snap: Boolean,
    proxyLine: Int,
    selectedControl: Int,
    selectedBackground: Int,
    unselectedControl: Int,
    unselectedBackground: Int,
  ): Boolean {
    val frameTime = System.currentTimeMillis()
    var invalidate = false
    val currentUnselectedBackground =
      if (proxyLine == 1) Color.TRANSPARENT else unselectedBackground

    if (proxy.type.group) {
      title = proxy.name

      if (link == null) {
        subtitle = proxy.type.name
      } else {
        if (linkNow !== link.now) {
          linkNow = link.now

          subtitle = "%s(%s)".format(proxy.type.name, link.now.ifEmpty { "*" })
        }
      }
    } else {
      title = proxy.title
      subtitle = proxy.subtitle
    }

    if (delay != proxy.delay) {
      delay = proxy.delay
      delayText = if (proxy.delay in 0..Short.MAX_VALUE) proxy.delay.toString() else ""
    }

    if (parentNow !== parent.now) {
      parentNow = parent.now
      selected = proxy.name == parent.now
    }

    controls = if (selected) selectedControl else unselectedControl

    if (snap) {
      background = if (selected) selectedBackground else currentUnselectedBackground
    } else {
      val target = if (selected) selectedBackground else currentUnselectedBackground

      if (background != target) {
        val sa = Color.alpha(background)
        val sr = Color.red(background)
        val sg = Color.green(background)
        val sb = Color.blue(background)

        val ta = Color.alpha(target)
        val tr = Color.red(target)
        val tg = Color.green(target)
        val tb = Color.blue(target)

        val da = ta - sa
        val dr = tr - sr
        val dg = tg - sg
        val db = tb - sb

        val max =
          max(da.absoluteValue, max(dr.absoluteValue, max(dg.absoluteValue, db.absoluteValue)))

        val frameOffset = frameTime - lastFrameTime

        val colorOffset = (frameOffset / max.toFloat().coerceAtLeast(0.001f)).coerceIn(0.0f, 1.0f)

        background =
          if (colorOffset > 0.999f) {
            target
          } else {
            Color.argb(
              (sa + da * colorOffset).toInt(),
              (sr + dr * colorOffset).toInt(),
              (sg + dg * colorOffset).toInt(),
              (sb + db * colorOffset).toInt(),
            )
          }

        invalidate = true
      }
    }

    lastFrameTime = frameTime

    return invalidate
  }
}
