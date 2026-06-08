package com.github.kr328.clash.common.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.github.kr328.clash.common.packageName as pkgName
import kotlin.reflect.KClass
import org.koin.core.context.GlobalContext

val Class<*>.componentName: ComponentName
  get() = ComponentName(pkgName, name)

val KClass<*>.componentName: ComponentName
  get() = ComponentName(pkgName, this.java.name)

val KClass<*>.intent: Intent
  get() = Intent(GlobalContext.get().get<Context>(), this.java)
