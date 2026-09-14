package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val number: String,
    val name: String? = null,
    val callType: Int, // 1: Incoming, 2: Outgoing, 3: Missed, 4: Voicemail, 5: Rejected, 6: Blocked
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0,
    val simSlot: Int = 0, // 0 for SIM 1, 1 for SIM 2
    val isRead: Boolean = true
) {
    companion object {
        const val TYPE_INCOMING = 1
        const val TYPE_OUTGOING = 2
        const val TYPE_MISSED = 3
        const val TYPE_VOICEMAIL = 4
        const val TYPE_REJECTED = 5
        const val TYPE_BLOCKED = 6
    }
}
