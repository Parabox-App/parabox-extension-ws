package com.ojhdtapp.parabox.extension.ws.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChatMapping(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val slaveOriginUid: String,
    val slaveMsgId: String
)
