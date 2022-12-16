package com.ojhdtapp.parabox.extension.ws.remote.message_content

import android.content.Context
import com.ojhdtapp.parabox.extension.ws.core.util.FileUtil
import com.ojhdtapp.parabox.extension.ws.core.util.FileUtil.toDateAndTimeString
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.MessageContent
import java.io.IOException

interface EFBMessageContent {
    companion object {
        const val TEXT = 0
        const val IMAGE = 1
        const val VOICE = 2
        const val AUDIO = 3
        const val FILE = 4
    }

    fun toMessageContent(context: Context): MessageContent
}

fun MessageContent.toEFBMessageContent(context: Context): EFBMessageContent {
    return when (this) {
        is com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.PlainText -> {
            EFBText(text = this.text)
        }
        is com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.Image -> {
            val b64String = this.uri?.let { FileUtil.uri2ByteStr(context = context, it) }
            EFBImage(
                b64String = b64String ?: throw IOException("Image uri is null"),
            )
        }
        is com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.Audio -> {
            val b64String = this.uri?.let { FileUtil.uri2ByteStr(context = context, it) }
            EFBAudio(
                b64String = b64String ?: throw IOException("Audio uri is null"),
                fileName = this.fileName ?: "Audio_${
                    System.currentTimeMillis().toDateAndTimeString()
                }"
            )
        }
        is com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.File -> {
            val b64String = this.uri?.let { FileUtil.uri2ByteStr(context = context, it) }
            EFBFile(
                b64String = b64String ?: throw IOException("File uri is null"),
                fileName = this.name
            )
        }
        else -> throw IOException("MessageContent type is not supported")
    }
}