package com.github.kr328.clash.util

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

const val CLASH_WIKI = "https://github.com/Dreamacro/clash/wiki"
const val CLASH_META_WIKI = "https://docs.metacubex.one/"
const val CLASH_META_CORE = "https://github.com/MetaCubeX/Clash.Meta"
const val TABBY_GITHUB = "https://github.com/Goooler/Tabby"

fun Context.openLink(link: String) {
  startActivity(Intent(Intent.ACTION_VIEW).setData(link.toUri()))
}
