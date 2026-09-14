package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.BlockedNumberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedNumberDao {
    @Query("SELECT * FROM blocked_numbers ORDER BY timestamp DESC")
    fun getAllBlockedNumbers(): Flow<List<BlockedNumberEntity>>

    @Query("SELECT COUNT(*) > 0 FROM blocked_numbers WHERE number = :number")
    suspend fun isNumberBlocked(number: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedNumber(blocked: BlockedNumberEntity): Long

    @Query("DELETE FROM blocked_numbers WHERE number = :number")
    suspend fun deleteBlockedNumber(number: String)

    @Query("DELETE FROM blocked_numbers WHERE id = :id")
    suspend fun deleteBlockedNumberById(id: Long)
}
