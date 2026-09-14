package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.VoicemailEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoicemailDao {
    @Query("SELECT * FROM voicemails ORDER BY timestamp DESC")
    fun getAllVoicemails(): Flow<List<VoicemailEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoicemail(voicemail: VoicemailEntity): Long

    @Query("UPDATE voicemails SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("DELETE FROM voicemails WHERE id = :id")
    suspend fun deleteVoicemail(id: Long)
}
