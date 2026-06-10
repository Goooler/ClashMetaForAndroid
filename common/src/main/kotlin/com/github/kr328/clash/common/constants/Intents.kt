package com.github.kr328.clash.common.constants

import com.github.kr328.clash.common.util.packageName

object Intents {
  // Public
  val ACTION_PROVIDE_URL = "$packageName.intent.action.PROVIDE_URL"
  val ACTION_START_CLASH = "$packageName.intent.action.START_CLASH"
  val ACTION_STOP_CLASH = "$packageName.intent.action.STOP_CLASH"
  val ACTION_TOGGLE_CLASH = "$packageName.intent.action.TOGGLE_CLASH"

  // Routes
  val ACTION_PROPERTIES = "$packageName.intent.action.PROPERTIES"
  val ACTION_LOGCAT = "$packageName.intent.action.LOGCAT"
  val ACTION_APK_BROKEN = "$packageName.intent.action.APK_BROKEN"
  val ACTION_APP_CRASHED = "$packageName.intent.action.APP_CRASHED"

  // Self
  val ACTION_SERVICE_RECREATED = "$packageName.intent.action.CLASH_RECREATED"
  val ACTION_CLASH_STARTED = "$packageName.intent.action.CLASH_STARTED"
  val ACTION_CLASH_STOPPED = "$packageName.intent.action.CLASH_STOPPED"
  val ACTION_CLASH_REQUEST_STOP = "$packageName.intent.action.CLASH_REQUEST_STOP"
  val ACTION_PROFILE_CHANGED = "$packageName.intent.action.PROFILE_CHANGED"
  val ACTION_PROFILE_UPDATE_COMPLETED = "$packageName.intent.action.PROFILE_UPDATE_COMPLETED"
  val ACTION_PROFILE_UPDATE_FAILED = "$packageName.intent.action.PROFILE_UPDATE_FAILED"
  val ACTION_PROFILE_REQUEST_UPDATE = "$packageName.intent.action.PROFILE_REQUEST_UPDATE"
  val ACTION_PROFILE_SCHEDULE_UPDATES = "$packageName.intent.action.SCHEDULE_UPDATES"
  val ACTION_PROFILE_LOADED = "$packageName.intent.action.PROFILE_LOADED"
  val ACTION_OVERRIDE_CHANGED = "$packageName.intent.action.OVERRIDE_CHANGED"

  const val EXTRA_NAME = "name"
  const val EXTRA_STOP_REASON = "stop_reason"
  const val EXTRA_UUID = "uuid"
  const val EXTRA_FAIL_REASON = "fail_reason"
}
