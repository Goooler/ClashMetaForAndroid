package com.github.kr328.clash.service.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.TypeConverters
import kotlin.uuid.Uuid

@Entity(
  tableName = "selections",
  foreignKeys =
    [
      ForeignKey(
        entity = Imported::class,
        childColumns = ["uuid"],
        parentColumns = ["uuid"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE,
      )
    ],
  primaryKeys = ["uuid", "proxy"],
)
@TypeConverters(RoomTypeConverters::class)
data class Selection(val uuid: Uuid, val proxy: String, val selected: String)
