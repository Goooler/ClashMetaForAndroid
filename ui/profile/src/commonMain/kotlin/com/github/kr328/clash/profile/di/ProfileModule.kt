package com.github.kr328.clash.profile.di

import com.github.kr328.clash.profile.vm.FilesViewModel
import com.github.kr328.clash.profile.vm.NewProfileViewModel
import com.github.kr328.clash.profile.vm.ProfilesViewModel
import com.github.kr328.clash.profile.vm.PropertiesViewModel
import com.github.kr328.clash.profile.vm.ProvidersViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val profileModule = module {
  viewModelOf(::PropertiesViewModel)
  viewModelOf(::FilesViewModel)
  viewModelOf(::NewProfileViewModel)
  viewModelOf(::ProfilesViewModel)
  viewModelOf(::ProvidersViewModel)
}
