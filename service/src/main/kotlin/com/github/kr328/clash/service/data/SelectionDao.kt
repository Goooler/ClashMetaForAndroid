package com.github.kr328.clash.service.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import java.util.UUID

@Dao
@TypeConverters(Converters::class)
interface SelectionDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE) fun setSelected(selection: Selection)

  @Query("DELETE FROM selections WHERE uuid = :uuid AND proxy = :proxy")
  fun removeSelected(uuid: UUID, proxy: String)

  @Query("SELECT * FROM selections WHERE uuid = :uuid")
  suspend fun querySelections(uuid: UUID): List<Selection>

  @Query("DELETE FROM selections WHERE uuid = :uuid AND proxy in (:proxies)")
  suspend fun removeSelections(uuid: UUID, proxies: List<String>)
}
