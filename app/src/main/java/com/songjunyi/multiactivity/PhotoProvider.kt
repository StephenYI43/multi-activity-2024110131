package com.songjunyi.multiactivity

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.File
import java.io.FileNotFoundException

/** A narrow, non-exported provider: only grants access to photos in our private directory. */
class PhotoProvider : ContentProvider() {
    override fun onCreate() = true
    private fun file(uri: Uri): File {
        val ctx = context ?: throw FileNotFoundException()
        val root = File(ctx.filesDir, "photos").canonicalFile
        val name = uri.lastPathSegment ?: throw FileNotFoundException()
        val file = File(root, name).canonicalFile
        if (uri.authority != ctx.packageName + ".photos" || file.parentFile != root ||
            !name.startsWith("capture_") || !name.endsWith(".jpg") || !file.isFile)
            throw FileNotFoundException()
        return file
    }
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor =
        ParcelFileDescriptor.open(file(uri), ParcelFileDescriptor.parseMode(mode))
    override fun getType(uri: Uri) = "image/jpeg"
    override fun query(uri: Uri, projection: Array<out String>?, selection: String?,
                       selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
        val f = file(uri)
        val columns = projection ?: arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        return MatrixCursor(columns).apply {
            addRow(columns.map<String, Any?> { if (it == OpenableColumns.DISPLAY_NAME) f.name
                else if (it == OpenableColumns.SIZE) f.length() else null }.toTypedArray())
        }
    }
    override fun insert(uri: Uri, values: ContentValues?): Uri? = throw UnsupportedOperationException()
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<out String>?) = 0
}

