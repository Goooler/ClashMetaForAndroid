package com.github.kr328.clash

import com.github.kr328.clash.design.HelpDesign
import kotlinx.coroutines.isActive

class HelpActivity : BaseActivity<HelpDesign>() {
  override suspend fun main() {
    setContentDesign(HelpDesign(this))

    while (isActive) {
      events.receive()
    }
  }
}
