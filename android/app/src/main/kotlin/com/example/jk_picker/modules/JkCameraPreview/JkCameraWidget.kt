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
    private val cameraProviderFuture = ProcessCameraProvider.getInstance(context)


    // Sử dụng MutableState để quản lý trạng thái quyền
    private var hasCameraPermission by mutableStateOf(false)

    init {
        // Kiểm tra quyền ngay khi khởi tạo
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Nếu đã có quyền, cập nhật trạng thái
            hasCameraPermission = true
        } else {
            // Nếu chưa có, yêu cầu quyền.
            // Phương thức này cần được triển khai để cập nhật `hasCameraPermission` sau khi có kết quả
            PermissionHandler().requestCameraPermission(activity)
        }

        cameraView.setViewTreeLifecycleOwner(lifecycleOwner)
        cameraView.setContent {
            // UI sẽ tự động cập nhật khi `hasCameraPermission` thay đổi
            if (hasCameraPermission) {
                JkCameraView(cameraProviderFuture)
            } else {
                // Hiển thị UI khi không có quyền
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