package com.github.kr328.clash.service.data

import androidx.room.ColumnInfo
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
data class Selection(
  @ColumnInfo(name = "uuid") val uuid: Uuid,
  @ColumnInfo(name = "proxy") val proxy: String,
  @ColumnInfo(name = "selected") val selected: String,
)
