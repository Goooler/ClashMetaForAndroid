package com.github.kr328.clash.log.di

import com.github.kr328.clash.log.vm.LogcatViewModel
import com.github.kr328.clash.log.vm.LogsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val logModule = module {
  viewModelOf(::LogcatViewModel)
  viewModelOf(::LogsViewModel)
}
