package com.github.kr328.clash.home

import com.github.kr328.clash.home.vm.AndroidDependencies
import com.github.kr328.clash.home.vm.HomeViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {
  single<HomeViewModel.Dependencies> { AndroidDependencies(androidApplication()) }
  viewModelOf(::HomeViewModel)
}
