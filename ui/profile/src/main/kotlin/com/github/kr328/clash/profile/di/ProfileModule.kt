package com.github.kr328.clash.profile.di

import com.github.kr328.clash.profile.vm.PropertiesViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val profileModule = module {
  viewModelOf(::PropertiesViewModel)
}
