package com.github.kr328.clash.service.di

import com.github.kr328.clash.service.data.Database
import org.koin.dsl.module

val serviceModule = module {
  single<Database> { Database.open(get()) }
}
