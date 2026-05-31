package com.github.kr328.clash.glue.util

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

const val MIHOMO_CORE = "https://github.com/MetaCubeX/mihomo
const val MIHOMO_WIKI = "https://wiki.metacubex.one/"
const val TABBY_REPO = "Goooler/Tabby"
const val TABBY_GITHUB = "https://github.com/$TABBY_REPO"
const val TABBY_RELEASES_LATEST = "$TABBY_GITHUB/releases/latest"

fun Context.openLink(link: String) {
  CustomTabsIntent.Builder().build().launchUrl(this, link.toUri())
}
