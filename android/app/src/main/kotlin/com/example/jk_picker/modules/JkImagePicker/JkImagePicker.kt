package com.example.jk_picker.modules.JkImagePicker

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.jk_picker.modules.PermissionHandler
import com.example.jk_picker.utils.AppConstant
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

object JkImagePicker {
    fun initMethodChannel(flutterEngine: FlutterEngine, context: Context) {
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            AppConstant.Companion.CHANNEL_JK_PICKER
        ).setMethodCallHandler { call: MethodCall, result: MethodChannel.Result ->
            when (call.method) {
                AppConstant.Companion.METHOD_GET_ALBUMS -> {
                    if (!PermissionHandler().hasPermission(context as Activity)) {
                        PermissionHandler().requestImagePermission(context)
                        result.error("Permission Denied", "Image permission not granted", null)
                    } else {
                        val listAlbums = fetchAlbums(context)
                        result.success(listAlbums)
                    }
                }

                AppConstant.Companion.METHOD_GET_IMAGE -> {}
                AppConstant.Companion.METHOD_GET_ASSETSINALBUM -> {}
                AppConstant.Companion.METHOD_CAMERA_PERMISSION -> {
                    if (PermissionHandler().hasCameraPermission(context as Activity)) {
                        result.success(true)
                    } else {
                        PermissionHandler().requestCameraPermission(context)
                        result.success(false) // Flutter sẽ đợi được xin quyền
                    }
                }

                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    fun fetchAlbums(context: Context): List<String> {
        val albums = mutableListOf<String>();
        val projection = arrayOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        val imagesUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        context.contentResolver.query(imagesUri, projection, null, null, null)?.use { cursor ->
            val bucketDisplayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val album = cursor.getString(bucketDisplayNameColumn)
                if (!albums.contains(album)) {
                    albums.add(album)
                }
            }
        }

        return albums
    }

    fun fetchImagesByAlbum(context: Context): Map<String, List<String>> {
        val albumMap = mutableMapOf<String, MutableList<String>>()

        val projection =
            arrayOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA)

        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val cursor = context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )

        cursor?.use {
            val bucketColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (it.moveToNext()) {
                val albumName = it.getString(bucketColumn)
                val imagePath = it.getString(dataColumn)

                if (albumMap.containsKey(albumName)) {
                    albumMap[albumName]?.add(imagePath)
                } else {
                    albumMap[albumName] = mutableListOf(imagePath)
                }
            }
        }

        return albumMap
    }

    fun checkAndRequestPermissions(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_MEDIA_VIDEO
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun fetchAllImages(context: Context): List<String> {
        val images = mutableListOf<String>()
        val mapAlbums = fetchImagesByAlbum(context)
        for (album in mapAlbums) {
            images.addAll(album.value)
        }
        return images
    }

    fun fetchAllImageBitmap(context: Context): List<Bitmap> {
        val images = mutableListOf<Bitmap>()
        val listImages = fetchAllImages(context)

        for (image in listImages) {
            try {
                val bitmap = BitmapFactory.decodeFile(image)
                images.add(bitmap)
            } catch (e: Exception) {
                // Handle exceptions, e.g., if the URI is invalid or the image doesn't exist
                e.printStackTrace()
                null
            }
        }
        return images
    }
}