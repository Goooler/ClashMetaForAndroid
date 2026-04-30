package com.github.kr328.clash.service.util

import com.github.kr328.clash.service.data.ImportedDao
import com.github.kr328.clash.service.data.PendingDao
import kotlin.uuid.Uuid

suspend fun generateProfileUUID(): Uuid {
  var result = Uuid.random()

  while (ImportedDao().exists(result) || PendingDao().exists(result)) {
    result = Uuid.random()
  }

  return result
}
