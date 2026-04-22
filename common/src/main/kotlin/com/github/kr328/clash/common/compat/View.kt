@file:Suppress("DEPRECATION")

package com.github.kr328.clash.common.compat

import android.widget.TextView
import androidx.annotation.StyleRes

var TextView.textAppearance: Int
  get() = throw UnsupportedOperationException("set value only")
  set(@StyleRes value) {
    setTextAppearance(value)
  }
