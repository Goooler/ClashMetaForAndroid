@file:Suppress("DEPRECATION")

package com.github.kr328.clash.common.compat

import android.content.res.Configuration
import java.util.*

val Configuration.preferredLocale: Locale
    get() = locales[0]
