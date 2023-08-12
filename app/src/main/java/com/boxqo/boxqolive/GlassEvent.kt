package com.boxqo.boxqolive

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat")
data class GlassEvent(

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val type: String,
    val action: String = "",
    val message: String = ""
)