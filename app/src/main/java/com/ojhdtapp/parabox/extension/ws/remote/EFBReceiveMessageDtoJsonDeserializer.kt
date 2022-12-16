package com.ojhdtapp.parabox.extension.ws.remote

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.ojhdtapp.parabox.extension.ws.remote.dto.EFBProfile
import com.ojhdtapp.parabox.extension.ws.remote.dto.EFBReceiveMessageDto
import com.ojhdtapp.parabox.extension.ws.remote.message_content.*
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.PluginConnection
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.Profile
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.ReceiveMessageDto
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.MessageContent
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.PlainText
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.At
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.AtAll
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.Audio
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.File
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.Image
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.QuoteReply
import java.lang.reflect.Type

class EFBReceiveMessageDtoJsonDeserializer : JsonDeserializer<EFBReceiveMessageDto> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): EFBReceiveMessageDto? {
        return json?.asJsonObject?.let {
            val contents = mutableListOf<EFBMessageContent>()
            if (it.has("contents")) {
                contents.addAll(
                    getMessageContentList(it.get("contents").asJsonArray)
                )
            }
            val profile = if (it.has("profile")) {
                val profileObj = it.get("profile").asJsonObject
                EFBProfile(
                    profileObj.get("name").asString,
                    profileObj.get("avatar").asString
                )
            } else null
            val subjectProfile = if (it.has("subjectProfile")) {
                val profileObj = it.get("subjectProfile").asJsonObject
                EFBProfile(
                    profileObj.get("name").asString,
                    profileObj.get("avatar").asString
                )
            } else null
            val timestamp = it.get("timestamp").asLong
            val chatType = it.get("chatType").asInt
            val slaveOriginUid = it.get("slaveOriginUid").asString
            val slaveMsgId = it.get("slaveMsgId").asString
            if (profile != null && subjectProfile != null) {
                EFBReceiveMessageDto(
                    contents,
                    profile,
                    subjectProfile,
                    timestamp,
                    chatType,
                    slaveOriginUid,
                    slaveMsgId
                )
            } else null
        }
    }

    private fun getMessageContentList(
        jsonArr: JsonArray,
        withoutQuoteReply: Boolean = false
    ): List<EFBMessageContent> {
        val contents = mutableListOf<EFBMessageContent>()
        jsonArr.forEach { content ->
            content.asJsonObject.let { contentObject ->
                if (contentObject.has("type")) {
                    val type = contentObject.get("type").asInt
                    when (type) {
                        EFBMessageContent.TEXT -> {
                            contents.add(
                                EFBText(
                                    contentObject.get("text").asString
                                )
                            )
                        }
                        EFBMessageContent.IMAGE -> {
                            contents.add(
                                EFBImage(
                                    contentObject.get("b64String").asString
                                )
                            )
                        }
                        EFBMessageContent.AUDIO -> {
                            contents.add(
                                EFBAudio(
                                    b64String = contentObject.get("b64String").asString,
                                    fileName = contentObject.get("fileName").asString
                                )
                            )
                        }
                        EFBMessageContent.VOICE -> {
                            contents.add(
                                EFBVoice(
                                    b64String = contentObject.get("b64String").asString,
                                    fileName = contentObject.get("fileName").asString
                                )
                            )
                        }
                        EFBMessageContent.FILE -> {
                            contents.add(
                                EFBFile(
                                    b64String = contentObject.get("b64String").asString,
                                    fileName = contentObject.get("fileName").asString
                                )
                            )
                        }
                    }
                }
            }
        }
        return contents
    }
}