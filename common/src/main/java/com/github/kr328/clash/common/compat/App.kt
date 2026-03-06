package com.github.kr328.clash.common.compat

import android.app.Application
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build

val Application.currentProcessName: String
    get() = Application.getProcessName()

fun Drawable.foreground(): Drawable {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        this is AdaptiveIconDrawable && this.background == null
    ) {
        return this.foreground
    }
    return this
}
