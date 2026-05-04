package com.github.kr328.clash.model

import android.graphics.drawable.Drawable

data class AppInfo(
  val packageName: String,
  val label: String,
  val icon: Drawable,
  val installTime: Long,
  val updateDate: Long,
) {
  enum class Sorter(comparator: Comparator<AppInfo>) : Comparator<AppInfo> by comparator {
    Label(compareBy(AppInfo::label)),
    PackageName(compareBy(AppInfo::packageName)),
    InstallTime(compareBy(AppInfo::installTime)),
    UpdateTime(compareBy(AppInfo::updateDate)),
  }
}
