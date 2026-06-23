package com.example.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

data class PickedFile(
    val uri: Uri,
    val name: String,
    val type: FileType
)

enum class FileType { IMAGE, PDF, UNKNOWN }

fun getFileType(uri: Uri, context: Context): FileType {
    val mimeType = context.contentResolver
        .getType(uri) ?: return FileType.UNKNOWN
    return when {
        mimeType.startsWith("image/") -> FileType.IMAGE
        mimeType == "application/pdf" -> FileType.PDF
        else -> FileType.UNKNOWN
    }
}

fun copyUriToInternalStorage(
    context: Context,
    uri: Uri,
    fileName: String
): String? {
    return try {
        val dir = File(context.filesDir, "ZeroBook")
        dir.mkdirs()
        val destFile = File(dir, fileName)
        context.contentResolver
            .openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        destFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun rememberImagePickerLauncher(
    onResult: (Uri?) -> Unit
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
) { uri -> onResult(uri) }

@Composable
fun rememberDocumentPickerLauncher(
    onResult: (Uri?) -> Unit
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
) { uri -> onResult(uri) }

@Composable
fun rememberCameraLauncher(
    context: Context,
    onResult: (Uri?) -> Unit
): Pair<androidx.activity.result.ActivityResultLauncher<Uri>, Uri> {
    val photoFile = remember {
        File(context.cacheDir, 
             "photo_${System.currentTimeMillis()}.jpg")
    }
    val photoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            photoFile
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        onResult(if (success) photoUri else null)
    }
    return Pair(launcher, photoUri)
}
