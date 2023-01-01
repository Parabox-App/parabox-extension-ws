package com.ojhdtapp.parabox.extension.ws.core.util

import android.content.Context
import android.graphics.*
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.content.FileProvider
import com.ojhdtapp.parabox.extension.ws.BuildConfig
import java.io.*
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


object FileUtil {
    fun String.toSafeFilename(): String{
        return this.replace("[\\\\/:*?\"<>|]".toRegex(), "_")
    }

    fun uri2ByteStr(context: Context, uri: Uri): String? {
        with(context) {
            val inputPFD: ParcelFileDescriptor? =
                contentResolver.openFileDescriptor(uri, "r")
            val fd = inputPFD!!.fileDescriptor
            val inputStream = FileInputStream(fd)
            inputStream.use {
                val bytes = it.readBytes()
                return Base64.getEncoder().encodeToString(bytes)
            }
            inputPFD.close()
        }
    }

    fun byteStr2Bitmap(byteStr: String): Bitmap? {
        val bytes = Base64.getDecoder().decode(byteStr)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    fun byteStr2File(context: Context, byteStr: String, fileName: String): File {
        val bytes = Base64.getDecoder().decode(byteStr)
        val targetDir = File(context.externalCacheDir, "file")
        if (!targetDir.exists()) targetDir.mkdirs()
        val tempFile =
            File(targetDir, fileName)
        ByteArrayInputStream(bytes).use { bis ->
            FileOutputStream(tempFile).use { fos ->
                bis.copyTo(fos, DEFAULT_BUFFER_SIZE)
            }
        }
        return tempFile
    }

    fun Bitmap.getCircledBitmap(): Bitmap {
        val output = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(0, 0, this.width, this.height)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(this.width / 2f, this.height / 2f, this.width / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(this, rect, rect, paint)
        return output
    }

    fun getUriFromBitmap(context: Context, bm: Bitmap, name: String): Uri? {
        val targetDir = File(context.externalCacheDir, "bm")
        if (!targetDir.exists()) targetDir.mkdirs()
        val tempFile =
            File(targetDir, "temp_${name}.png")
        val bytes = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, bytes)
        val bitmapData = bytes.toByteArray()
        FileOutputStream(tempFile).use { fos ->
            fos.write(bitmapData)
        }
        return getUriOfFile(context, tempFile)
    }

    fun getUriOfFile(context: Context, file: File): Uri? {
        return try {
            FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".provider", file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun Long.toFormattedDate(): String {
        return SimpleDateFormat("M'月'd'日'", Locale.getDefault()).format(Date(this))
    }

    fun Long.toDateAndTimeString(): String {
        return SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss-",
            Locale.getDefault()
        ).format(Date(this)) + this.toString().substring(11)
    }

    fun Long.toMSString(): String {
        val df = DecimalFormat("#").apply {
            roundingMode = RoundingMode.DOWN
        }
        val totalSecond = (this / 1000).toFloat().roundToInt()
        val minute = df.format(totalSecond / 60)
        val second = totalSecond % 60
        return "${if (totalSecond > 60) minute.plus("′") else ""}${second}“"
    }
}