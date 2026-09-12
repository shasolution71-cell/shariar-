package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object PhotoStorageUtil {
    fun savePhotoToInternal(context: Context, uri: Uri, memberId: String): String {
        return try {
            val photosDir = File(context.filesDir, "member_photos").apply {
                if (!exists()) mkdirs()
            }
            val sanitizedId = memberId.replace(Regex("[^a-zA-Z0-9_]"), "_")
            val destFile = File(photosDir, "photo_${sanitizedId}_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (_: Exception) {
            uri.toString()
        }
    }
}
