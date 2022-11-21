package com.ojhdtapp.parabox.extension.ws.remote

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
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

class ReceiveMessageDtoJsonDeserializer : JsonDeserializer<ReceiveMessageDto> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ReceiveMessageDto? {
        return json?.asJsonObject?.let {
            val contents = mutableListOf<MessageContent>()
            if (it.has("contents")) {
                contents.addAll(
                    getMessageContentList(it.get("contents").asJsonArray)
                )
            }
            val profile = if (it.has("profile")) {
                val profileObj = it.get("profile").asJsonObject
                Profile(
                    profileObj.get("name").asString,
                    profileObj.get("avatar").asString,
                    profileObj.get("id").asLong,
                    null
                )
            } else null
            val subjectProfile = if (it.has("subjectProfile")) {
                val profileObj = it.get("subjectProfile").asJsonObject
                Profile(
                    profileObj.get("name").asString,
                    profileObj.get("avatar").asString,
                    profileObj.get("id").asLong,
                    null
                )
            } else null
            val timestamp = it.get("timestamp").asLong
            val messageId = it.get("messageId")?.asLong
            val pluginConnection = if (it.has("pluginConnection")) {
                val pluginConnectionObj = it.get("pluginConnection").asJsonObject
                PluginConnection(
                    pluginConnectionObj.get("connectionType").asInt,
                    pluginConnectionObj.get("sendTargetType").asInt,
                    pluginConnectionObj.get("id").asLong
                )
            } else null
            if (profile != null && subjectProfile != null && pluginConnection != null) {
                ReceiveMessageDto(
                    contents,
                    profile,
                    subjectProfile,
                    timestamp,
                    messageId,
                    pluginConnection
                )
            } else null
        }
    }

    private fun getMessageContentList(
        jsonArr: JsonArray,
        withoutQuoteReply: Boolean = false
    ): List<MessageContent> {
        val contents = mutableListOf<MessageContent>()
        jsonArr.forEach { content ->
            content.asJsonObject.let { contentObject ->
                if (contentObject.has("type")) {
                    val type = contentObject.get("type").asInt
                    when (type) {
                        MessageContent.AT -> {
                            val target = contentObject.get("target").asLong
                            val name = contentObject.get("name").asString
                            contents.add(At(target, name))
                        }

                        MessageContent.AT_ALL -> {
                            contents.add(AtAll)
                        }

                        MessageContent.AUDIO -> {
                            val url = contentObject.get("url").asString
                            val length = contentObject.get("length").asLong
                            val fileName = contentObject.get("fileName").asString
                            val fileSize = contentObject.get("fileSize").asLong
                            val uri = null
                            contents.add(Audio(url, length, fileName, fileSize, uri))
                        }

                        MessageContent.FILE -> {
                            val url = contentObject.get("url").asString
                            val name = contentObject.get("name").asString
                            val extension = contentObject.get("extension").asString
                            val size = contentObject.get("size").asLong
                            val lastModifiedTime = contentObject.get("lastModifiedTime").asLong
                            val expiryTime = contentObject.get("expiryTime").asLong
                            val uri = null
                            contents.add(
                                File(
                                    url,
                                    name,
                                    extension,
                                    size,
                                    lastModifiedTime,
                                    expiryTime,
                                    uri
                                )
                            )
                        }

                        MessageContent.IMAGE -> {
                            val url = contentObject.get("url").asString
                            val width = contentObject.get("width").asInt
                            val height = contentObject.get("height").asInt
                            val uri = null
                            contents.add(Image(url, width, height, uri))
                        }

                        MessageContent.PLAIN_TEXT -> {
                            val text = contentObject.get("text").asString
                            contents.add(PlainText(text))
                        }

                        MessageContent.QUOTE_REPLY -> {
                            if (!withoutQuoteReply) {
                                val quoteMessageSenderName =
                                    contentObject.get("quoteMessageSenderName").asString
                                val quoteMessageTimestamp =
                                    contentObject.get("quoteMessageTimestamp").asLong
                                val quoteMessageId = contentObject.get("quoteMessageId").asLong
                                val quoteMessageContent =
                                    contentObject.get("quoteMessageContent").asJsonArray
                                contents.add(
                                    QuoteReply(
                                        quoteMessageSenderName,
                                        quoteMessageTimestamp,
                                        quoteMessageId,
                                        getMessageContentList(quoteMessageContent, true)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        return contents
    }
}