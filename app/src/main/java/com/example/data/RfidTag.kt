package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rfid_tags")
data class RfidTag(
    @PrimaryKey val uid: String,
    val title: String,
    val streamUrl: String,
    val isAuthorized: Boolean = true,
    val streamResolution: String = "4K UHD",
    val lastScanned: Long = 0L,
    val category: String = "Video Stream"
)
