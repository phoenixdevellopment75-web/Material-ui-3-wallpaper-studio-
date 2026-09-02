package com.example.export

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.engine.ProceduralRenderer
import com.example.engine.WallpaperParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

enum class WallpaperTarget(val label: String) {
    HOME_SCREEN("Home Screen"),
    LOCK_SCREEN("Lock Screen"),
    BOTH("Home & Lock Screen")
}

sealed class ExportResult {
    data class SavedToGallery(val uri: Uri, val path: String) : ExportResult()
    data class AppliedToSystem(val target: WallpaperTarget) : ExportResult()
    data class Shared(val intent: Intent) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

object WallpaperExporter {

    /**
     * Renders offscreen at high resolution and saves to device gallery using MediaStore API.
     */
    suspend fun saveToGallery(
        context: Context,
        params: WallpaperParams,
        targetWidth: Int = params.aspectRatio.defaultExportWidth,
        targetHeight: Int = params.aspectRatio.defaultExportHeight
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val bitmap = ProceduralRenderer.renderToBitmap(targetWidth, targetHeight, params)
            val filename = "Wallpaper_${params.patternType.name.lowercase()}_${System.currentTimeMillis()}.png"

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Wallpapers")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return@withContext ExportResult.Error("Failed to initialize MediaStore image URI")

            resolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            } ?: return@withContext ExportResult.Error("Failed to write bitmap to storage stream")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            bitmap.recycle()
            ExportResult.SavedToGallery(uri, "Pictures/Wallpapers/$filename")
        } catch (e: Exception) {
            ExportResult.Error(e.localizedMessage ?: "Unknown export error occurred")
        }
    }

    /**
     * Sets the rendered wallpaper directly on the device using WallpaperManager.
     */
    suspend fun applyAsSystemWallpaper(
        context: Context,
        params: WallpaperParams,
        target: WallpaperTarget,
        targetWidth: Int = params.aspectRatio.defaultExportWidth,
        targetHeight: Int = params.aspectRatio.defaultExportHeight
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            val bitmap = ProceduralRenderer.renderToBitmap(targetWidth, targetHeight, params)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val whichFlag = when (target) {
                    WallpaperTarget.HOME_SCREEN -> WallpaperManager.FLAG_SYSTEM
                    WallpaperTarget.LOCK_SCREEN -> WallpaperManager.FLAG_LOCK
                    WallpaperTarget.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                }
                wallpaperManager.setBitmap(bitmap, null, true, whichFlag)
            } else {
                wallpaperManager.setBitmap(bitmap)
            }

            bitmap.recycle()
            ExportResult.AppliedToSystem(target)
        } catch (e: Exception) {
            ExportResult.Error(e.localizedMessage ?: "Failed to set system wallpaper")
        }
    }

    /**
     * Exports and triggers Android system share sheet.
     */
    suspend fun createShareIntent(
        context: Context,
        params: WallpaperParams
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val bitmap = ProceduralRenderer.renderToBitmap(
                params.aspectRatio.defaultExportWidth,
                params.aspectRatio.defaultExportHeight,
                params
            )

            val cacheDir = File(context.cacheDir, "shared_wallpapers").apply { mkdirs() }
            val file = File(cacheDir, "wallpaper_${System.currentTimeMillis()}.png")

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            bitmap.recycle()

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            ExportResult.Shared(intent)
        } catch (e: Exception) {
            ExportResult.Error(e.localizedMessage ?: "Failed to prepare share file")
        }
    }
}
