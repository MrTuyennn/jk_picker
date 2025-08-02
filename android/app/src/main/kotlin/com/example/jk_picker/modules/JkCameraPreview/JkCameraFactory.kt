package com.example.jk_picker.modules.JkCameraPreview

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import com.example.jk_picker.modules.PermissionHandler
import com.example.jk_picker.utils.AppConstant
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class JkCameraFactory(private val activity: Activity, private val lifecycleOwner: LifecycleOwner):PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        return JkCameraWidget(activity, context, lifecycleOwner)

    }

}

