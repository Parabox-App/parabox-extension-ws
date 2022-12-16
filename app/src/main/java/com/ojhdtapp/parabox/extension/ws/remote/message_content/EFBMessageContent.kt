package com.ojhdtapp.parabox.extension.ws.remote.message_content

import android.content.Context
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.MessageContent

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