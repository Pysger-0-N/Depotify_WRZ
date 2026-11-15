package com.radityaf.depotify_wrz.utils

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

object ExportUtils {
    fun saveCsvToDownloads(context: Context, fileName: String, csvContent: String): String {
        var savedPath = ""
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(csvContent.toByteArray(Charsets.UTF_8))
                    }
                    savedPath = "Folder Downloads/$fileName"

                    // Index file for MediaScanner / Recent apps (MIUI / HyperOS File Manager)
                    try {
                        val projection = arrayOf(MediaStore.Downloads.DATA)
                        resolver.query(uri, projection, null, null, null)?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                val columnIndex = cursor.getColumnIndex(MediaStore.Downloads.DATA)
                                if (columnIndex != -1) {
                                    val filePath = cursor.getString(columnIndex)
                                    if (!filePath.isNullOrBlank()) {
                                        MediaScannerConnection.scanFile(context, arrayOf(filePath), arrayOf("text/csv"), null)
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { fos ->
                    fos.write(csvContent.toByteArray(Charsets.UTF_8))
                }
                savedPath = file.absolutePath

                // Force media scanner to index file so it appears in "Recent" (MIUI File Manager)
                MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("text/csv"), null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal mengekspor file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
        return savedPath
    }
}
