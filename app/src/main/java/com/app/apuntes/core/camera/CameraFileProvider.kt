package com.app.apuntes.core.camera

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

private const val IMAGE_DIRECTORY = "images"

fun Context.createTempImageUri(): Uri {
    val imageDirectory = File(cacheDir, IMAGE_DIRECTORY).apply {
        if (!exists()) {
            mkdirs()
        }
    }

    val imageFile = File.createTempFile(
        "ocr_capture_",
        ".jpg",
        imageDirectory
    )

    return FileProvider.getUriForFile(
        this,
        "$packageName.fileprovider",
        imageFile
    )
}
