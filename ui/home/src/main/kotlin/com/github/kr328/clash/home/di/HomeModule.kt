package com.github.kr328.clash.home.di

import com.github.kr328.clash.home.vm.AndroidDependencies
import com.github.kr328.clash.home.vm.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {
  single<HomeViewModel.Dependencies> { AndroidDependencies(application = get()) }
  viewModelOf(::HomeViewModel)
}
