package com.github.kr328.clash.service.data

import android.content.Context
import androidx.room3.Database as DB
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.TypeConverter
import androidx.room3.TypeConverters
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.service.model.Profile
import java.lang.ref.SoftReference
import kotlin.uuid.Uuid

@DB(
  version = 1,
  entities = [Imported::class, Pending::class, Selection::class],
  exportSchema = false,
)
@TypeConverters(RoomTypeConverters::class)
abstract class Database : RoomDatabase() {
  abstract fun importedDao(): ImportedDao

  abstract fun pendingDao(): PendingDao

  abstract fun selectionProxyDao(): SelectionDao

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
        .build()
    }
  }
}

object RoomTypeConverters {
  @TypeConverter
  fun fromUUID(uuid: Uuid): String {
    return uuid.toString()
  }

  @TypeConverter
  fun toUUID(uuid: String): Uuid {
    return Uuid.parse(uuid)
  }

  @TypeConverter
  fun fromProfileType(type: Profile.Type): String {
    return type.name
  }

  @TypeConverter
  fun toProfileType(type: String): Profile.Type {
    return Profile.Type.valueOf(type)
  }
}
