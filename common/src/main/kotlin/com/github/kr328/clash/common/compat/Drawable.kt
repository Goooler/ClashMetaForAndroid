package com.github.kr328.clash.common.compat

import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable

val Drawable.foreground: Drawable
  get() {
    if (this is AdaptiveIconDrawable && this.background == null) {
      return this.foreground
    }
    return this
  }
