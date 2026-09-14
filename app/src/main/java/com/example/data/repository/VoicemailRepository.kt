package com.example.data.repository

import com.example.data.local.dao.VoicemailDao
import com.example.data.local.entity.VoicemailEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class VoicemailRepository(private val voicemailDao: VoicemailDao) {
    val allVoicemails: Flow<List<VoicemailEntity>> = voicemailDao.getAllVoicemails()

    suspend fun markAsRead(id: Long) = withContext(Dispatchers.IO) {
        voicemailDao.markAsRead(id)
    }

    suspend fun deleteVoicemail(id: Long) = withContext(Dispatchers.IO) {
        voicemailDao.deleteVoicemail(id)
    }

    suspend fun insertVoicemail(voicemail: VoicemailEntity): Long = withContext(Dispatchers.IO) {
        voicemailDao.insertVoicemail(voicemail)
    }
}
