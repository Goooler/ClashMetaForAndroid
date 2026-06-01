package com.github.kr328.clash.service.data

import android.content.Context
import androidx.room3.Database as DB
import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.service.data.migrations.LEGACY_MIGRATION
import com.github.kr328.clash.service.data.migrations.MIGRATIONS
import java.lang.ref.SoftReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@DB(
  version = 1,
  entities = [Imported::class, Pending::class, Selection::class],
  exportSchema = false,
)
abstract class Database : RoomDatabase() {
  abstract fun openImportedDao(): ImportedDao

  abstract fun openPendingDao(): PendingDao

  abstract fun openSelectionProxyDao(): SelectionDao

  companion object {
    val database: Database
      @Synchronized
      get() {
        return softDatabase.get()
          ?: open(Global.application).apply { softDatabase = SoftReference(this) }
      }

    private var softDatabase: SoftReference<Database?> = SoftReference(null)

    private fun open(context: Context): Database {
      return Room.databaseBuilder(context.applicationContext, Database::class.java, "profiles")
        .addMigrations(*MIGRATIONS)
        .build()
    }

    init {
      Global.launch(Dispatchers.IO) { LEGACY_MIGRATION(Global.application) }
    }
  }
}
