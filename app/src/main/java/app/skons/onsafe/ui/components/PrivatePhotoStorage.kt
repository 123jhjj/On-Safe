package app.skons.onsafe.ui.components

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

object PrivatePhotoStorage {

    private fun picturesDir(context: Context): File =
        File(context.cacheDir, "pictures").apply { mkdirs() }

    fun createCaptureUri(context: Context): Uri? {
        val file = File(picturesDir(context), "onsafe_${System.currentTimeMillis()}.jpg")
        return try {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    fun copyToPrivate(context: Context, source: Uri): Uri? {
        val file = File(picturesDir(context), "onsafe_${System.currentTimeMillis()}.jpg")
        return try {
            context.contentResolver.openInputStream(source)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            } ?: run {
                file.delete()
                return null
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (_: IOException) {
            file.delete()
            null
        } catch (_: SecurityException) {
            file.delete()
            null
        } catch (_: IllegalArgumentException) {
            file.delete()
            null
        }
    }

    fun delete(context: Context, uri: Uri?) {
        if (uri == null || uri.authority != "${context.packageName}.fileprovider") return
        try {
            context.contentResolver.delete(uri, null, null)
        } catch (_: SecurityException) {
        } catch (_: IllegalArgumentException) {
        }
    }
}
