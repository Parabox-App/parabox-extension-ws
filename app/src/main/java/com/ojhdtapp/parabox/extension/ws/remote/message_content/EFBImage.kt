package com.ojhdtapp.parabox.extension.ws.remote.message_content

import android.content.Context
import android.content.Intent
import com.ojhdtapp.parabox.extension.ws.core.util.FileUtil
import com.ojhdtapp.parabox.extension.ws.core.util.FileUtil.toDateAndTimeString
import com.ojhdtapp.parabox.extension.ws.core.util.FileUtil.toSafeFilename
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.Image
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.MessageContent

data class EFBImage(
    val b64String: String, val fileName: String,
    override val type: Int = EFBMessageContent.IMAGE
) : EFBMessageContent {
    override fun toMessageContent(context: Context): MessageContent {
        val name = System.currentTimeMillis().toDateAndTimeString()
        val bm = if (b64String.isBlank()) null
        else FileUtil.byteStr2Bitmap(b64String)
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
        return Image(
            uri = uri,
            fileName = fileName
        )
    }
}
