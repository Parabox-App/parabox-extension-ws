package com.ojhdtapp.parabox.extension.ws.remote.dto

import com.ojhdtapp.parabox.extension.ws.domain.model.ChatMapping
import com.ojhdtapp.parabox.extension.ws.domain.service.ConnService
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.PluginConnection
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.Profile
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.ReceiveMessageDto
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.MessageContent

data class EFBReceiveMessageDto(
    val contents: List<MessageContent>,
    val profile: Profile,
    val subjectProfile: Profile,
    val timestamp: Long,
    val chatType: Int,
    val slaveOriginUid: String,
    val slaveMsgId: String,
){
    fun getChatMapping(): ChatMapping = ChatMapping(
        slaveOriginUid = slaveOriginUid,
        slaveMsgId = slaveMsgId
    )

    fun toReceiveMessageDto(chatMappingId: Long): ReceiveMessageDto{
        return ReceiveMessageDto(
            contents = contents,
            profile = profile,
            subjectProfile = subjectProfile,
            timestamp = timestamp,
            messageId = null,
            pluginConnection = PluginConnection(
                connectionType = ConnService.connectionType,
                sendTargetType = chatType,
                id = chatMappingId
            )
        )
    }
}
