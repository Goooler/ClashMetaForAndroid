package com.github.kr328.clash.settings.di

import com.github.kr328.clash.settings.vm.AccessControlViewModel
import com.github.kr328.clash.settings.vm.AppSettingsViewModel
import com.github.kr328.clash.settings.vm.MetaFeatureSettingsViewModel
import com.github.kr328.clash.settings.vm.NetworkSettingsViewModel
import com.github.kr328.clash.settings.vm.OverrideSettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule = module {
  viewModelOf(::AccessControlViewModel)
  viewModelOf(::AppSettingsViewModel)
  viewModelOf(::MetaFeatureSettingsViewModel)
  viewModelOf(::NetworkSettingsViewModel)
  viewModelOf(::OverrideSettingsViewModel)
}
