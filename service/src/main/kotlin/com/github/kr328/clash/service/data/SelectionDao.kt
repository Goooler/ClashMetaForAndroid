package com.github.kr328.clash.service.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import kotlin.uuid.Uuid

@Dao
@TypeConverters(Converters::class)
interface SelectionDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE) fun setSelected(selection: Selection)

  @Query("DELETE FROM selections WHERE uuid = :uuid AND proxy = :proxy")
  fun removeSelected(uuid: Uuid, proxy: String)

  @Query("SELECT * FROM selections WHERE uuid = :uuid")
  suspend fun querySelections(uuid: Uuid): List<Selection>

  @Query("DELETE FROM selections WHERE uuid = :uuid AND proxy in (:proxies)")
  suspend fun removeSelections(uuid: Uuid, proxies: List<String>)
}
