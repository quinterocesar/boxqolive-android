package com.boxqo.boxqolive

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat")
data class GlassEvent(

    @PrimaryKey(autoGenerate = true)
    var id: String = "",
    val deviceName: String,
    val type: String,
    val action: String = "",
    val message: String = ""
)