package com.github.kr328.clash.glue.util

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

const val CLASH_WIKI = "https://github.com/Dreamacro/clash/wiki"
const val CLASH_META_WIKI = "https://docs.metacubex.one/"
const val CLASH_META_CORE = "https://github.com/MetaCubeX/Clash.Meta"
const val TABBY_REPO = "Goooler/Tabby"
const val TABBY_GITHUB = "https://github.com/$TABBY_REPO"
const val TABBY_RELEASES_LATEST = "$TABBY_GITHUB/releases/latest"

fun Context.openLink(link: String) {
  CustomTabsIntent.Builder().build().launchUrl(this, link.toUri())
}
