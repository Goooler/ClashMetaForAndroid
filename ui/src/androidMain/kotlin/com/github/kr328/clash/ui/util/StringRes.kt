package com.github.kr328.clash.ui.util

import android.content.Context
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource

fun Context.getString(res: Any, vararg formatArgs: Any): String {
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

@Composable
fun stringResCompat(res: Any, vararg formatArgs: Any): String {
  return when (res) {
    is StringResource -> org.jetbrains.compose.resources.stringResource(res, *formatArgs)
    is Int -> androidx.compose.ui.res.stringResource(res, *formatArgs)
    else -> throw IllegalArgumentException("Unsupported resource type: $res")
  }
}
