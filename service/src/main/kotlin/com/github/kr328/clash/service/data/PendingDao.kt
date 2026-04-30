package com.github.kr328.clash.service.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import androidx.room.Update
import kotlin.uuid.Uuid

@Dao
@TypeConverters(RoomTypeConverters::class)
interface PendingDao {
  @Query("SELECT * FROM pending WHERE uuid = :uuid") suspend fun queryByUUID(uuid: Uuid): Pending?

  @Query("DELETE FROM pending WHERE uuid = :uuid") suspend fun remove(uuid: Uuid)

  @Query("SELECT EXISTS(SELECT 1 FROM pending WHERE uuid = :uuid)")
  suspend fun exists(uuid: Uuid): Boolean

  @Query("SELECT uuid FROM pending ORDER BY createdAt") suspend fun queryAllUUIDs(): List<Uuid>

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(pending: Pending)

  @Update(onConflict = OnConflictStrategy.REPLACE) suspend fun update(pending: Pending)
}
