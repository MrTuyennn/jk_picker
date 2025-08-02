package com.example.jk_picker.modules.JkCameraPreview

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import com.example.jk_picker.modules.PermissionHandler
import io.flutter.plugin.platform.PlatformView

class JkCameraWidget(activity: Activity, context: Context, lifecycleOwner: LifecycleOwner) : PlatformView {
    private val cameraView = ComposeView(context)

    init {
        cameraView.setViewTreeLifecycleOwner(lifecycleOwner)
        cameraView.setContent {
            if(PermissionHandler().hasCameraPermission(activity)){
                JkCameraView()
            }else {
                PermissionHandler().requestCameraPermission(activity)
                View(context)
            }
        }
    }
    override fun getView(): View{
        return cameraView
    }
    override fun dispose() {
    }

}