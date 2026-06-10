package com.github.kr328.clash.ui.util

import android.content.Context
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource

fun Context.getString(res: Any): String {
  return when (res) {
    is Int -> getString(res)
    is StringResource -> {
      val id = resources.getIdentifier(res.key, "string", packageName)
      if (id == 0) {
        throw IllegalArgumentException("Resource not found for key: ${res.key}")
      }
      getString(id)
    }
    else -> throw IllegalArgumentException("Unsupported resource type: $res")
  }
}

fun Context.getString(res: Any, vararg formatArgs: Any?): String {
  return when (res) {
    is Int -> getString(res, *formatArgs)
    is StringResource -> {
      val id = resources.getIdentifier(res.key, "string", packageName)
      if (id == 0) {
        throw IllegalArgumentException("Resource not found for key: ${res.key}")
      }
      getString(id, *formatArgs)
    }
    else -> throw IllegalArgumentException("Unsupported resource type: $res")
  }
}

fun Context.getStringId(res: Any): Int {
  return when (res) {
    is Int -> res
    is StringResource -> {
      val id = resources.getIdentifier(res.key, "string", packageName)
      if (id == 0) {
        throw IllegalArgumentException("Resource not found for key: ${res.key}")
      }
      id
    }
    else -> throw IllegalArgumentException("Unsupported resource type: $res")
  }
}

@Composable
fun stringResource(res: Any, vararg formatArgs: Any?): String {
  val nonNullArgs = formatArgs.map { it ?: "null" }.toTypedArray()
  return when (res) {
    is StringResource -> org.jetbrains.compose.resources.stringResource(res, *nonNullArgs)
    is Int -> androidx.compose.ui.res.stringResource(res, *nonNullArgs)
    else -> throw IllegalArgumentException("Unsupported resource type: $res")
  }
}
