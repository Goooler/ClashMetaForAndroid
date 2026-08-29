package com.github.kr328.clash.service.clash.module

import android.app.Service
import co.touchlab.kermit.Logger
import com.github.kr328.clash.common.constants.Intents

class CloseModule(service: Service) : Module<CloseModule.RequestClose>(service) {
  object RequestClose

  override suspend fun run() {
    val broadcasts = receiveBroadcast { addAction(Intents.ACTION_CLASH_REQUEST_STOP) }

    broadcasts.receive()

    Logger.d("User request close")

    return enqueueEvent(RequestClose)
  }
}
