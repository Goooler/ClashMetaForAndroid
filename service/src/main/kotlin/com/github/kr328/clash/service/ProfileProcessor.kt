package com.github.kr328.clash.service

import android.content.Context
import androidx.core.net.toUri
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.service.data.Imported
import com.github.kr328.clash.service.data.ImportedDao
import com.github.kr328.clash.service.data.Pending
import com.github.kr328.clash.service.data.PendingDao
import com.github.kr328.clash.service.model.Profile
import com.github.kr328.clash.service.remote.IFetchObserver
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.service.util.fetchSubscriptionUserInfo
import com.github.kr328.clash.service.util.importedDir
import com.github.kr328.clash.service.util.pendingDir
import com.github.kr328.clash.service.util.processingDir
import com.github.kr328.clash.service.util.sendProfileChanged
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

object ProfileProcessor {
  private val profileLock = Mutex()
  private val processLock = Mutex()

  suspend fun apply(context: Context, uuid: Uuid, callback: IFetchObserver? = null) {
    withContext(NonCancellable) {
      processLock.withLock {
        val snapshot = profileLock.withLock {
          val pending =
            PendingDao().queryByUUID(uuid)
              ?: throw IllegalArgumentException("profile $uuid not found")

          pending.enforceFieldValid()

          context.processingDir.deleteRecursively()
          context.processingDir.mkdirs()

          context.pendingDir
            .resolve(pending.uuid.toString())
            .copyRecursively(context.processingDir, overwrite = true)

          pending
        }

        Clash.setAgeSecretKey(snapshot.ageSecretKey?.takeIf { it.isNotBlank() })

        val force = snapshot.type != Profile.Type.File
        var cb = callback

        Clash.fetchAndValid(context.processingDir, snapshot.source, force) {
            try {
              cb?.updateStatus(it)
            } catch (e: Exception) {
              cb = null

              Log.w("Report fetch status: $e", e)
            }
          }
          .await()

        profileLock.withLock {
          if (PendingDao().queryByUUID(snapshot.uuid) == snapshot) {
            context.importedDir.resolve(snapshot.uuid.toString()).deleteRecursively()
            context.processingDir.copyRecursively(
              context.importedDir.resolve(snapshot.uuid.toString())
            )

            val old = ImportedDao().queryByUUID(snapshot.uuid)
            if (snapshot.type == Profile.Type.Url) {
              val userInfo =
                if (snapshot.source.startsWith("https://", true)) {
                  context.fetchSubscriptionUserInfo(snapshot.source)
                } else {
                  null
                }
              val new =
                Imported(
                  snapshot.uuid,
                  snapshot.name,
                  snapshot.type,
                  snapshot.source,
                  snapshot.interval,
                  userInfo?.upload ?: 0,
                  userInfo?.download ?: 0,
                  userInfo?.total ?: 0,
                  userInfo?.expire ?: 0,
                  old?.createdAt ?: System.currentTimeMillis(),
                  ageSecretKey = snapshot.ageSecretKey,
                )
              if (old != null) {
                ImportedDao().update(new)
              } else {
                ImportedDao().insert(new)
              }

              PendingDao().remove(snapshot.uuid)

              context.pendingDir.resolve(snapshot.uuid.toString()).deleteRecursively()

              context.sendProfileChanged(snapshot.uuid)
            } else if (snapshot.type == Profile.Type.File) {
              val new =
                Imported(
                  snapshot.uuid,
                  snapshot.name,
                  snapshot.type,
                  snapshot.source,
                  snapshot.interval,
                  0,
                  0,
                  0,
                  0,
                  old?.createdAt ?: System.currentTimeMillis(),
                  ageSecretKey = snapshot.ageSecretKey,
                )
              if (old != null) {
                ImportedDao().update(new)
              } else {
                ImportedDao().insert(new)
              }

              PendingDao().remove(snapshot.uuid)

              context.pendingDir.resolve(snapshot.uuid.toString()).deleteRecursively()

              context.sendProfileChanged(snapshot.uuid)
            }
          }
        }
      }
    }
  }

  suspend fun update(context: Context, uuid: Uuid, callback: IFetchObserver?) {
    withContext(NonCancellable) {
      processLock.withLock {
        val snapshot = profileLock.withLock {
          val imported =
            ImportedDao().queryByUUID(uuid)
              ?: throw IllegalArgumentException("profile $uuid not found")

          context.processingDir.deleteRecursively()
          context.processingDir.mkdirs()

          context.importedDir
            .resolve(imported.uuid.toString())
            .copyRecursively(context.processingDir, overwrite = true)

          imported
        }

        Clash.setAgeSecretKey(snapshot.ageSecretKey?.takeIf { it.isNotBlank() })

        var cb = callback

        Clash.fetchAndValid(context.processingDir, snapshot.source, true) {
            try {
              cb?.updateStatus(it)
            } catch (e: Exception) {
              cb = null

              Log.w("Report fetch status: $e", e)
            }
          }
          .await()

        profileLock.withLock {
          if (ImportedDao().exists(snapshot.uuid)) {
            context.importedDir.resolve(snapshot.uuid.toString()).deleteRecursively()
            context.processingDir.copyRecursively(
              context.importedDir.resolve(snapshot.uuid.toString())
            )

            context.sendProfileChanged(snapshot.uuid)
          }
        }
      }
    }
  }

  suspend fun delete(context: Context, uuid: Uuid) {
    withContext(NonCancellable) {
      profileLock.withLock {
        ImportedDao().remove(uuid)
        PendingDao().remove(uuid)

        val pending = context.pendingDir.resolve(uuid.toString())
        val imported = context.importedDir.resolve(uuid.toString())

        pending.deleteRecursively()
        imported.deleteRecursively()

        context.sendProfileChanged(uuid)
      }
    }
  }

  suspend fun release(context: Context, uuid: Uuid): Boolean {
    return withContext(NonCancellable) {
      profileLock.withLock {
        PendingDao().remove(uuid)

        context.pendingDir.resolve(uuid.toString()).deleteRecursively()
      }
    }
  }

  suspend fun active(context: Context, uuid: Uuid) {
    withContext(NonCancellable) {
      profileLock.withLock {
        if (ImportedDao().exists(uuid)) {
          val store = ServiceStore(context)

          store.activeProfile = uuid

          context.sendProfileChanged(uuid)
        }
      }
    }
  }

  private fun Pending.enforceFieldValid() {
    val scheme = source.toUri().scheme?.lowercase(Locale.getDefault())

    when {
      name.isBlank() -> throw IllegalArgumentException("Empty name")

      source.isEmpty() && type != Profile.Type.File -> throw IllegalArgumentException("Invalid url")

      source.isNotEmpty() && scheme != "https" && scheme != "http" && scheme != "content" ->
        throw IllegalArgumentException("Unsupported url $source")

      interval != 0L && interval.milliseconds.inWholeMinutes < 15 ->
        throw IllegalArgumentException("Invalid interval")
    }
  }
}
