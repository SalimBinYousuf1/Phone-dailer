package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voicemails")
data class VoicemailEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val number: String,
    val name: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 28,
    val transcript: String = "",
    val isRead: Boolean = false
)
