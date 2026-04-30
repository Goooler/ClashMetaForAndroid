package com.github.kr328.clash.service.data

import androidx.room.TypeConverter
import com.github.kr328.clash.service.model.Profile
import kotlin.uuid.Uuid

class Converters {
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
