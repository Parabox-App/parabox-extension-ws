package com.ojhdtapp.parabox.extension.ws.remote.message_content

import android.content.Context
import android.content.Intent
import com.ojhdtapp.parabox.extension.ws.core.util.FileUtil
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.Image
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.MessageContent

data class EFBAnimation(
    val b64String: String,
    val fileName: String, override val type: Int = EFBMessageContent.ANIMATION
) : EFBMessageContent {
    override fun toMessageContent(context: Context): MessageContent {
        val file = FileUtil.byteStr2File(context, b64String, fileName)
        val uri = FileUtil.getUriOfFile(context, file).apply {
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
        return Image(
            uri = uri,
            fileName = fileName
        )
    }
}
