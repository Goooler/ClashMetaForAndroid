package com.github.kr328.clash.ui

import android.app.ActivityManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.content.getSystemService
import com.github.kr328.clash.store.UiStore

abstract class BaseActivity : ComponentActivity() {
  protected val uiStore by lazy { UiStore(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Apply excludeFromRecents setting to all app tasks.
    checkNotNull(getSystemService<ActivityManager>()).appTasks.forEach { task ->
      task.setExcludeFromRecents(uiStore.hideFromRecents)
    }
  }
}
