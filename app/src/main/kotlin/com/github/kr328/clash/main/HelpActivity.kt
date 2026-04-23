package com.github.kr328.clash.main

import com.github.kr328.clash.main.ui.HelpDesign
import com.github.kr328.clash.ui.BaseActivity
import kotlinx.coroutines.isActive

class HelpActivity : BaseActivity<HelpDesign>() {
  override suspend fun main() {
    setContentDesign(HelpDesign(this))

    while (isActive) {
      events.receive()
    }
  }
}
