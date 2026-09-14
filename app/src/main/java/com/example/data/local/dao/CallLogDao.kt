package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.CallLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getAllCallLogs(): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs WHERE callType = :type ORDER BY timestamp DESC")
    fun getCallLogsByType(type: Int): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs WHERE number LIKE '%' || :query || '%' OR (name IS NOT NULL AND name LIKE '%' || :query || '%') ORDER BY timestamp DESC")
    fun searchCallLogs(query: String): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs WHERE number = :number ORDER BY timestamp DESC")
    fun getCallLogsForNumber(number: String): Flow<List<CallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(callLog: CallLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLogs(callLogs: List<CallLogEntity>)

    @Delete
    suspend fun deleteCallLog(callLog: CallLogEntity)

    @Query("DELETE FROM call_logs WHERE id IN (:ids)")
    suspend fun deleteCallLogsByIds(ids: List<Long>)

    @Query("DELETE FROM call_logs")
    suspend fun clearAllCallLogs()

    @Query("SELECT COUNT(*) FROM call_logs")
    suspend fun getCallLogsCount(): Int
}
