package com.github.kr328.clash.common.util

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

fun Context.openLink(link: String) {
  CustomTabsIntent.Builder().build().launchUrl(this, link.toUri())
}
