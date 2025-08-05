package com.example.jk_picker.modules.JkCameraPreview

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import com.example.jk_picker.modules.PermissionHandler
import io.flutter.plugin.platform.PlatformView

class JkCameraWidget(
    private val activity: Activity,
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) : PlatformView {

    private val cameraView = ComposeView(context)


    // Sử dụng MutableState để quản lý trạng thái quyền
    private var hasCameraPermission by mutableStateOf(false)

    init {
        if (PermissionHandler().hasCameraPermission(activity)) {
            hasCameraPermission = true
        } else {
            PermissionHandler().requestCameraPermission(activity)
        }
        cameraView.setViewTreeLifecycleOwner(lifecycleOwner)
        cameraView.setContent {
            if (hasCameraPermission) {
                JkCameraView()
            } else {
                PermissionDeniedScreen {
                    PermissionHandler().requestCameraPermission(activity)
                }
            }
        }
    }

    override fun getView(): View = cameraView

    override fun dispose() {
        // ... Logic giải phóng tài nguyên
    }
}