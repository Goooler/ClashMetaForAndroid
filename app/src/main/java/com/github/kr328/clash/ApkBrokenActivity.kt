package com.github.kr328.clash

import com.github.kr328.clash.design.ApkBrokenDesign

class ApkBrokenActivity : BaseActivity<ApkBrokenDesign>() {
  override suspend fun main() {
    val design = ApkBrokenDesign(this)

    setContentDesign(design)
  }
}
