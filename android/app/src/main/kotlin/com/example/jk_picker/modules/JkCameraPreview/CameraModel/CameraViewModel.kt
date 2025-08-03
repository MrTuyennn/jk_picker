package com.example.jk_picker.modules.JkCameraPreview.CameraModel

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.datatransport.runtime.dagger.Component.Factory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.lang.Exception

data class CameraState(
   val capturedImage : Bitmap? = null
)

class CameraViewModel(private val savePhotoGalleryUseCase: SavePhotoToGalleryUseCase) :
    ViewModel() {
    private val _state = MutableStateFlow(CameraState())
    val state = _state.asStateFlow()

    fun storePhotoInGallery(bitmap: Bitmap){
        viewModelScope.launch {
            savePhotoGalleryUseCase.call(bitmap)
            updateCapturePhotoState(bitmap)
        }
    }

    private fun updateCapturePhotoState(bitmap: Bitmap){
        _state.value.capturedImage?.recycle()
        _state.value = _state.value.copy(capturedImage = bitmap)
    }

    override fun onCleared(){
        _state.value.capturedImage?.recycle()
        super.onCleared()
    }
}


@Factory
class SavePhotoToGalleryUseCase(private val context: Context) {
    suspend fun call(capturePhotoBitmap: Bitmap): Result<Unit> = withContext(Dispatchers.IO) {
        val resolver: ContentResolver = context.applicationContext.contentResolver
        val imageCollection: Uri = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            else -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val nowTimestamp: Long = System.currentTimeMillis()
        val imageContenValues: ContentValues = ContentValues().apply {

            put(MediaStore.Images.Media.DISPLAY_NAME, "Your image name" + ".jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.DATE_TAKEN, nowTimestamp)
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DCIM + "/YourAppNameOrAnyOtherSubFolderName"
                )
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                put(MediaStore.Images.Media.DATE_TAKEN, nowTimestamp)
                put(MediaStore.Images.Media.DATE_ADDED, nowTimestamp)
                put(MediaStore.Images.Media.DATE_MODIFIED, nowTimestamp)
                put(MediaStore.Images.Media.AUTHOR, "NguyenTrinh")
                put(MediaStore.Images.Media.DESCRIPTION, "My description")
            }

        }
        val imageMediaStore: Uri? = resolver.insert(imageCollection, imageContenValues)
        val result: Result<Unit> = imageMediaStore?.let { uri ->
            kotlin.runCatching {
                resolver.openOutputStream(uri).use { outputStream: OutputStream? ->
                    checkNotNull(outputStream) { "..." }
                    capturePhotoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    imageContenValues.clear()
                    imageContenValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, imageContenValues, null, null)
                }
                Result.success(Unit)
            }.getOrElse { exception ->
                exception.message?.let(:: println)
                resolver.delete(uri, null, null)
                Result.failure(exception)
            }
        } ?: run{
            Result.failure(Exception("Couldn't create file for gallery"))
        }
        return@withContext result
    }
}

