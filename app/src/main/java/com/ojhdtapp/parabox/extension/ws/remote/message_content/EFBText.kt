package com.ojhdtapp.parabox.extension.ws.remote.message_content

import android.content.Context
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.MessageContent
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.PlainText

data class EFBText(
    val text: String
) : EFBMessageContent {
    override fun toMessageContent(context: Context): MessageContent {
        return PlainText(
            text = text
        )
    }
}
