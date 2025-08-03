package com.example.jk_picker.modules.JkCameraPreview

import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.Window
import android.widget.LinearLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Future


@Composable
fun JkCameraView(cameraProviderFuture: ListenableFuture<ProcessCameraProvider>) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { ContextCompat.getMainExecutor(context) }


    // Sử dụng AndroidView để nhúng PreviewView từ CameraX
    AndroidView(
        factory = {
            // Tạo PreviewView chỉ một lần khi factory được gọi
            PreviewView(it).apply {
                layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                setBackgroundColor(android.graphics.Color.WHITE)
                scaleType = PreviewView.ScaleType.FIT_CENTER
            }
        },
        update = { previewView ->
            cameraProviderFuture.addListener({
                val cameraProvider = try {
                    cameraProviderFuture.get()
                } catch (e: Exception) {
                    Log.e("JkCameraView", "Failed to get camera provider", e)
                    return@addListener
                }

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                // ... (Logic bind camera như cũ)
                val backCameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        backCameraSelector,
                        preview
                    )
                } catch (e: Exception) {
                    Log.e("JkCameraView", "Binding camera failed", e)
                }

            }, executor)
        }
    )

    // Sử dụng DisposableEffect để đảm bảo camera được hủy liên kết khi rời khỏi màn hình
    DisposableEffect(lifecycleOwner) {
        onDispose {
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
        }
    }
}

// Thêm vào file JkCameraPreview.kt

@Composable
fun PermissionDeniedScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Ứng dụng cần quyền camera để hoạt động.",
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
        Button(onClick = onRequestPermission) {
            Text("Yêu cầu lại quyền")
        }
    }
}
