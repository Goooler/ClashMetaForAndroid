package com.github.kr328.clash.common

import android.app.Application
import org.koin.core.context.GlobalContext

val packageName: String
  get() = GlobalContext.get().get<Application>().packageName
