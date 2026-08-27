package com.example.plantidentifier.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

fun Uri.toFile(context: Context): File {
    val resolver = context.contentResolver
    val source = resolver.openInputStream(this)
        ?: throw IllegalArgumentException("Unable to open image")
    val bitmap = source.use { BitmapFactory.decodeStream(it) }
        ?: throw IllegalArgumentException("Unsupported image")

    return bitmap.toNormalizedJpeg(context, "plant_image_")
}

/** Converts an image to a bounded JPEG suitable for the PlantNet multipart API. */
fun File.normalizeJpeg(context: Context): File {
    val bitmap = BitmapFactory.decodeFile(absolutePath)
        ?: throw IllegalArgumentException("Unsupported image")

    return bitmap.toNormalizedJpeg(context, "plant_camera_").also {
        // The original camera capture is no longer needed after normalization.
        delete()
    }
}

private fun Bitmap.toNormalizedJpeg(context: Context, prefix: String): File {
    val file = File.createTempFile(prefix, ".jpg", context.cacheDir)

    // Normalize all gallery formats to a bounded JPEG payload. This prevents
    // HEIC/PNG files from being mislabeled as image/jpeg in the multipart request.
    val maxDimension = 2048
    val scale = minOf(1f, maxDimension.toFloat() / maxOf(width, height))
    val normalized = if (scale < 1f) {
        Bitmap.createScaledBitmap(
            this,
            (width * scale).toInt(),
            (height * scale).toInt(),
            true
        )
    } else this

    FileOutputStream(file).use { output ->
        check(normalized.compress(Bitmap.CompressFormat.JPEG, 88, output)) {
            "Unable to encode image"
        }
    }

    if (normalized !== this) normalized.recycle()
    recycle()

    return file
}
