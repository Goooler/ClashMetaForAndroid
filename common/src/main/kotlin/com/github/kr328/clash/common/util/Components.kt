package com.github.kr328.clash.common.util

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import com.github.kr328.clash.common.util.packageName as pkgName
import kotlin.reflect.KClass
import org.koin.core.context.GlobalContext

inline val Class<*>.componentName: ComponentName
  get() = ComponentName(pkgName, name)

inline val KClass<*>.componentName: ComponentName
  get() = ComponentName(pkgName, this.java.name)

inline val KClass<*>.intent: Intent
  get() = Intent().setComponent(componentName)

@PublishedApi
internal inline val packageName
  get() = GlobalContext.get().get<Application>().packageName
