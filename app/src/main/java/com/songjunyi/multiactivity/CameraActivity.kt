package com.songjunyi.multiactivity

import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import java.io.File

@Suppress("DEPRECATION")
class CameraActivity : LabActivity() {
    private var displayed: String? = null
    private var pending: String? = null
    private lateinit var preview: ImageView
    private lateinit var status: TextView
    private lateinit var details: TextView
    private lateinit var captureButton: Button
    private var bitmap: Bitmap? = null
    private fun photo(name: String) = File(File(filesDir, "photos"), name)
    private fun uri(name: String) = Uri.Builder().scheme("content")
        .authority(packageName + ".photos").appendPath(name).build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        displayed = savedInstanceState?.getString("displayed_photo")
        pending = savedInstanceState?.getString("pending_photo")
        screen(R.string.camera_heading, R.string.camera_subtitle)
        val panel = card()
        preview = ImageView(this).apply {
            id = R.id.photo_preview
            contentDescription = getString(R.string.photo_accessibility)
            scaleType = ImageView.ScaleType.FIT_CENTER
            background = shape(android.graphics.Color.rgb(8, 21, 37), 16f)
        }
        panel.addView(preview, LinearLayout.LayoutParams(-1, dp(260)))
        status = label(panel, R.string.photo_empty, 15f, accent)
        details = text(panel, getString(R.string.photo_empty), 12f, muted)
        captureButton = button(panel, R.string.take_photo).apply {
            id = R.id.take_photo
            setOnClickListener { capture() }
        }
        displayed?.let { displayPhoto(it) }
        footer(savedInstanceState != null)
    }

    private fun capture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) == null) {
            status.setText(R.string.camera_unavailable)
            return
        }
        try {
            val root = File(filesDir, "photos").apply { mkdirs() }
            val output = File.createTempFile("capture_", ".jpg", root)
            pending = output.name
            val outputUri = uri(output.name)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
            intent.clipData = ClipData.newRawUri(getString(R.string.photo_accessibility), outputUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(intent, Contract.CAMERA_REQUEST)
        } catch (_: Exception) {
            pending?.let { photo(it).delete() }
            pending = null
            status.setText(R.string.camera_error)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != Contract.CAMERA_REQUEST) return
        val name = pending ?: return
        revokeUriPermission(uri(name), Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        pending = null
        if (resultCode == RESULT_OK && displayPhoto(name)) {
            val old = displayed
            displayed = name
            if (old != null && old != name) photo(old).delete()
        } else {
            photo(name).delete()
            status.setText(if (resultCode == RESULT_OK) R.string.photo_failed else R.string.photo_cancelled)
        }
    }

    private fun displayPhoto(name: String): Boolean {
        return try {
            val file = photo(name)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return false
            val options = BitmapFactory.Options().apply {
                inSampleSize = 1
                while (maxOf(bounds.outWidth, bounds.outHeight) / inSampleSize > 1600) inSampleSize *= 2
            }
            val raw = BitmapFactory.decodeFile(file.absolutePath, options) ?: return false
            val matrix = Matrix()
            when (ExifInterface(file.absolutePath).getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.postRotate(90f); matrix.postScale(-1f, 1f) }
                ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.postRotate(270f); matrix.postScale(-1f, 1f) }
            }
            val shown = Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
            if (shown !== raw) raw.recycle()
            preview.setImageBitmap(shown)
            bitmap?.recycle()
            bitmap = shown
            status.setText(R.string.photo_success)
            details.text = getString(R.string.photo_details, bounds.outWidth, bounds.outHeight, file.length() / 1024)
            captureButton.setText(R.string.retake_photo)
            true
        } catch (_: Exception) { false }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("displayed_photo", displayed)
        outState.putString("pending_photo", pending)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        preview.setImageDrawable(null)
        bitmap?.recycle()
        bitmap = null
        super.onDestroy()
    }
}

