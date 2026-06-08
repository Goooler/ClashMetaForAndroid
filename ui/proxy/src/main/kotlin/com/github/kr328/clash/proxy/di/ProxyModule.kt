package com.github.kr328.clash.proxy.di

import com.github.kr328.clash.proxy.vm.ProxyViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val proxyModule = module {
  viewModelOf(::ProxyViewModel)
}
