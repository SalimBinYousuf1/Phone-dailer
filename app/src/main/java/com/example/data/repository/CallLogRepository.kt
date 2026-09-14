package com.example.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CallLog
import androidx.core.content.ContextCompat
import com.example.data.local.dao.CallLogDao
import com.example.data.local.entity.CallLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CallLogRepository(
    private val callLogDao: CallLogDao,
    private val context: Context
) {
    val allCallLogs: Flow<List<CallLogEntity>> = callLogDao.getAllCallLogs()

    fun getCallLogsByType(type: Int): Flow<List<CallLogEntity>> = callLogDao.getCallLogsByType(type)

    fun searchCallLogs(query: String): Flow<List<CallLogEntity>> = callLogDao.searchCallLogs(query)

    fun getCallLogsForNumber(number: String): Flow<List<CallLogEntity>> = callLogDao.getCallLogsForNumber(number)

    suspend fun insertCallLog(callLog: CallLogEntity): Long = withContext(Dispatchers.IO) {
        callLogDao.insertCallLog(callLog)
    }

    suspend fun deleteCallLog(callLog: CallLogEntity) = withContext(Dispatchers.IO) {
        callLogDao.deleteCallLog(callLog)
    }

    suspend fun deleteCallLogsByIds(ids: List<Long>) = withContext(Dispatchers.IO) {
        callLogDao.deleteCallLogsByIds(ids)
    }

    suspend fun clearAllCallLogs() = withContext(Dispatchers.IO) {
        callLogDao.clearAllCallLogs()
    }

    suspend fun syncSystemCallLog() = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return@withContext
        }

        try {
            val cursor: Cursor? = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION
                ),
                null,
                null,
                CallLog.Calls.DATE + " DESC LIMIT 100"
            )

            cursor?.use { c ->
                val numberIdx = c.getColumnIndex(CallLog.Calls.NUMBER)
                val nameIdx = c.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val typeIdx = c.getColumnIndex(CallLog.Calls.TYPE)
                val dateIdx = c.getColumnIndex(CallLog.Calls.DATE)
                val durIdx = c.getColumnIndex(CallLog.Calls.DURATION)

                val entries = mutableListOf<CallLogEntity>()
                while (c.moveToNext()) {
                    val number = if (numberIdx >= 0) c.getString(numberIdx) ?: "" else ""
                    val name = if (nameIdx >= 0) c.getString(nameIdx) else null
                    val rawType = if (typeIdx >= 0) c.getInt(typeIdx) else CallLog.Calls.INCOMING_TYPE
                    val date = if (dateIdx >= 0) c.getLong(dateIdx) else System.currentTimeMillis()
                    val duration = if (durIdx >= 0) c.getLong(durIdx) else 0L

                    val mappedType = when (rawType) {
                        CallLog.Calls.OUTGOING_TYPE -> CallLogEntity.TYPE_OUTGOING
                        CallLog.Calls.MISSED_TYPE -> CallLogEntity.TYPE_MISSED
                        CallLog.Calls.VOICEMAIL_TYPE -> CallLogEntity.TYPE_VOICEMAIL
                        CallLog.Calls.REJECTED_TYPE -> CallLogEntity.TYPE_REJECTED
                        CallLog.Calls.BLOCKED_TYPE -> CallLogEntity.TYPE_BLOCKED
                        else -> CallLogEntity.TYPE_INCOMING
                    }

                    if (number.isNotBlank()) {
                        entries.add(
                            CallLogEntity(
                                number = number,
                                name = name,
                                callType = mappedType,
                                timestamp = date,
                                durationSeconds = duration,
                                simSlot = 0
                            )
                        )
                    }
                }

                if (entries.isNotEmpty()) {
                    callLogDao.insertCallLogs(entries)
                }
            }
        } catch (_: Exception) {
            // Silently fallback to Room
        }
    }

    fun exportCsv(callLogs: List<CallLogEntity>): String {
        val sb = StringBuilder()
        sb.append("id,number,name,type,timestamp,duration_seconds,sim_slot\n")
        for (log in callLogs) {
            val typeStr = when (log.callType) {
                CallLogEntity.TYPE_INCOMING -> "INCOMING"
                CallLogEntity.TYPE_OUTGOING -> "OUTGOING"
                CallLogEntity.TYPE_MISSED -> "MISSED"
                CallLogEntity.TYPE_VOICEMAIL -> "VOICEMAIL"
                CallLogEntity.TYPE_REJECTED -> "REJECTED"
                CallLogEntity.TYPE_BLOCKED -> "BLOCKED"
                else -> "UNKNOWN"
            }
            sb.append("${log.id},\"${log.number}\",\"${log.name ?: ""}\",$typeStr,${log.timestamp},${log.durationSeconds},${log.simSlot}\n")
        }
        return sb.toString()
    }
}
