package com.ojhdtapp.parabox.extension.ws.remote.message_content

import android.content.Context
import android.util.Log
import com.ojhdtapp.parabox.extension.ws.core.util.FileUtil
import com.ojhdtapp.parabox.extension.ws.core.util.FileUtil.toDateAndTimeString
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.MessageContent
import java.io.IOException

interface EFBMessageContent {
    abstract val type: Int

    companion object {
        const val TEXT = 0
        const val IMAGE = 1
        const val VOICE = 2
        const val AUDIO = 3
        const val FILE = 4
        const val ANIMATION = 5
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
            Log.d("parabox", "image file name: ${this.fileName}")
            if (this.fileName?.endsWith(".gif") == true) {
                EFBAnimation(
                    b64String = b64String ?: throw IOException("Image uri is null"),
                    fileName = this.fileName!!
                )
            } else {
                EFBImage(
                    b64String = b64String ?: throw IOException("Image uri is null"),
                    fileName = this.fileName ?: "Image_${
                        System.currentTimeMillis().toDateAndTimeString()
                    }.jpg"
                )
            }
        }
        is com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.Audio -> {
            val b64String = this.uri?.let { FileUtil.uri2ByteStr(context = context, it) }
            EFBAudio(
                b64String = b64String ?: throw IOException("Audio uri is null"),
                fileName = this.fileName ?: "Audio_${
                    System.currentTimeMillis().toDateAndTimeString()
                }.mp3"
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