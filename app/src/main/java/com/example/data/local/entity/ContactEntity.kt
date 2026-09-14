package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val phoneLabel: String = "Mobile",
    val email: String = "",
    val company: String = "",
    val notes: String = "",
    val photoUri: String? = null,
    val isFavorite: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
)
