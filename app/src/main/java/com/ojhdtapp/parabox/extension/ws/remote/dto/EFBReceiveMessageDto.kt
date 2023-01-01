package com.ojhdtapp.parabox.extension.ws.remote.dto

import android.content.Context
import android.content.Intent
import com.ojhdtapp.parabox.extension.ws.core.util.FileUtil
import com.ojhdtapp.parabox.extension.ws.core.util.FileUtil.getCircledBitmap
import com.ojhdtapp.parabox.extension.ws.core.util.FileUtil.toSafeFilename
import com.ojhdtapp.parabox.extension.ws.domain.model.ChatMapping
import com.ojhdtapp.parabox.extension.ws.domain.service.ConnService
import com.ojhdtapp.parabox.extension.ws.remote.message_content.EFBMessageContent
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.PluginConnection
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.Profile
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.ReceiveMessageDto
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.MessageContent

data class EFBReceiveMessageDto(
    val contents: List<EFBMessageContent>,
    val profile: EFBProfile,
    val subjectProfile: EFBProfile,
    val timestamp: Long,
    val chatType: Int,
    val slaveOriginUid: String,
    val slaveMsgId: String,
) {
    fun getChatMapping(): ChatMapping = ChatMapping(
        slaveOriginUid = slaveOriginUid,
        slaveMsgId = slaveMsgId
    )

    fun toReceiveMessageDto(context: Context, chatMappingId: Long): ReceiveMessageDto {
        return ReceiveMessageDto(
            contents = contents.map {
                it.toMessageContent(context)
            },
            profile = profile.toProfile(context),
            subjectProfile = subjectProfile.toProfile(context),
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

data class EFBProfile(
    val name: String,
    val avatarB64Str: String?,
) {
    fun toProfile(context: Context): Profile {
        val bm = if (avatarB64Str.isNullOrBlank()) null
        else FileUtil.byteStr2Bitmap(avatarB64Str)?.getCircledBitmap()
        val uri = bm?.let { FileUtil.getUriFromBitmap(context, it, name.toSafeFilename()) }.apply {
            context.grantUriPermission(
                "com.ojhdtapp.parabox",
                this,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            context.grantUriPermission(
                "com.ojhdtapp.parabox",
                this,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }
        return Profile(
            name = name,
            avatar = null,
            id = null,
            avatarUri = uri
        )
    }
}