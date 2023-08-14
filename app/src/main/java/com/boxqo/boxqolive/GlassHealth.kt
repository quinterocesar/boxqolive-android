package com.boxqo.boxqolive

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health")
data class GlassHealth(

    @PrimaryKey(autoGenerate = true)
    var id: String = "",
    val name: String,
    val deviceName: String,
    val type: String,
    val isConnected: Boolean,
    val batteryLife: Int,
    val isStreaming: Boolean,
    val isAppOpen: Boolean
)