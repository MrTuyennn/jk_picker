package com.example.jk_picker.modules

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionHandler {
    private val IMAGE_PERMISSION_REQUEST_CODE = 2001
    private val CAMERA_PERMISSION_REQUEST_CODE = 2002

    fun hasPermission(activity: Activity): Boolean{
        return if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){
            ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_MEDIA_IMAGES)==PackageManager.PERMISSION_GRANTED

        }else {
            ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }

    }

    fun requestImagePermission(activity: Activity){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){
            ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),IMAGE_PERMISSION_REQUEST_CODE )

        }else {
            ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),IMAGE_PERMISSION_REQUEST_CODE )

        }
    }

    fun hasCameraPermission(activity: Activity):Boolean {
        return ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED
    }

    fun requestCameraPermission(activity: Activity){
        ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.CAMERA),CAMERA_PERMISSION_REQUEST_CODE)
    }
}