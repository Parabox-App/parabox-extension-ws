package com.ojhdtapp.parabox.extension.ws.remote.dto

import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.MessageContent

data class EFBSendMessageDto(
    val content: MessageContent,
    val timestamp: Long,
    val slaveOriginUid: String,
    val slaveMsgId: String,
    val messageId: Long
)
