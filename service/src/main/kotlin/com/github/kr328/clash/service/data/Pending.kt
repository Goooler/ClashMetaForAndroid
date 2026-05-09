package com.github.kr328.clash.service.data

import androidx.room.Entity
import androidx.room.TypeConverters
import com.github.kr328.clash.service.model.Profile
import kotlin.uuid.Uuid

@Entity(tableName = "pending", primaryKeys = ["uuid"])
@TypeConverters(RoomTypeConverters::class)
data class Pending(
  val uuid: Uuid,
  val name: String,
  val type: Profile.Type,
  val source: String,
  val interval: Long,
  val upload: Long,
  val download: Long,
  val total: Long,
  val expire: Long,
  val createdAt: Long = System.currentTimeMillis(),
)
