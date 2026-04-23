package com.github.kr328.clash.crash

import com.github.kr328.clash.crash.ui.ApkBrokenDesign
import com.github.kr328.clash.ui.BaseActivity

class ApkBrokenActivity : BaseActivity<ApkBrokenDesign>() {
  override suspend fun main() {
    val design = ApkBrokenDesign(this)

    setContentDesign(design)
  }
}
