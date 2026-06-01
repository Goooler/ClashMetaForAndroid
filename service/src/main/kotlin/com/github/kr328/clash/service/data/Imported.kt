package com.github.kr328.clash.service.data

import androidx.room3.Entity
import androidx.room3.TypeConverters
import com.github.kr328.clash.service.model.Profile
import kotlin.uuid.Uuid

@Entity(tableName = "imported", primaryKeys = ["uuid"])
@TypeConverters(RoomTypeConverters::class)
data class Imported(
  val uuid: Uuid,
  val name: String,
  val type: Profile.Type,
  val source: String,
  val interval: Long,
  val upload: Long,
  val download: Long,
  val total: Long,
  val expire: Long,
  val createdAt: Long,
)
