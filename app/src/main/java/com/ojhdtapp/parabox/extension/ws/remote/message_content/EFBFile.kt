package com.ojhdtapp.parabox.extension.ws.remote.message_content

import android.content.Context
import android.content.Intent
import com.ojhdtapp.parabox.extension.ws.core.util.FileUtil
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.File
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.MessageContent

data class EFBFile(
    val b64String: String,
    val fileName: String
) : EFBMessageContent{
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
        return File(
            name = fileName,
            extension = fileName.substringAfterLast("."),
            lastModifiedTime = file.lastModified(),
            size = file.length(),
            uri = uri
        )
    }
}
