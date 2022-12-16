package com.ojhdtapp.parabox.extension.ws.remote.dto

import com.ojhdtapp.parabox.extension.ws.remote.message_content.EFBMessageContent

data class EFBSendMessageDto(
    val content: EFBMessageContent,
    val timestamp: Long,
    val slaveOriginUid: String,
    val slaveMsgId: String,
    val messageId: Long
)
