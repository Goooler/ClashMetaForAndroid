package com.github.kr328.clash.crash.di

import com.github.kr328.clash.crash.vm.AppCrashedViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val crashModule = module {
  viewModelOf(::AppCrashedViewModel)
}
