package com.github.kr328.clash.crash

import com.github.kr328.clash.crash.ui.ApkBrokenDesign
import com.github.kr328.clash.ui.DesignActivity

class ApkBrokenActivity : DesignActivity<ApkBrokenDesign>() {
  override suspend fun main() {
    val design = ApkBrokenDesign(this)

    setContentDesign(design)
  }
}
