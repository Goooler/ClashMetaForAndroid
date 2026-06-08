package com.github.kr328.clash.service.data

import android.content.Context
import androidx.room3.Database as DB
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.TypeConverter
import androidx.room3.TypeConverters
import androidx.room3.migration.Migration
import androidx.sqlite.execSQL
import com.github.kr328.clash.service.model.Profile
import kotlin.uuid.Uuid
import org.koin.core.context.GlobalContext

@DB(
  version = 2,
  entities = [Imported::class, Pending::class, Selection::class],
  exportSchema = false,
)
@TypeConverters(RoomTypeConverters::class)
abstract class Database : RoomDatabase() {
  abstract fun importedDao(): ImportedDao

  abstract fun pendingDao(): PendingDao

  abstract fun selectionProxyDao(): SelectionDao

  companion object {
    val database: Database by lazy { GlobalContext.get().get() }

    private val MIGRATION_1_2 =
      Migration(1, 2) { db ->
        db.execSQL("ALTER TABLE imported ADD COLUMN ageSecretKey TEXT")
        db.execSQL("ALTER TABLE pending ADD COLUMN ageSecretKey TEXT")
      }

    internal fun open(context: Context): Database {
      return Room.databaseBuilder(context.applicationContext, Database::class.java, "profiles")
        .addMigrations(MIGRATION_1_2)
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
