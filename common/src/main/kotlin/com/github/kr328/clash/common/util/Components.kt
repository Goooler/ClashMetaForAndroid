package com.github.kr328.clash.common.util

import android.content.ComponentName
import android.content.Intent
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.packageName as pkgName
import kotlin.reflect.KClass

val Class<*>.componentName: ComponentName
  get() = ComponentName(pkgName, name)

val KClass<*>.componentName: ComponentName
  get() = ComponentName(pkgName, this.java.name)

val KClass<*>.intent: Intent
  get() = Intent(Global.application, this.java)
