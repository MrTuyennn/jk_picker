package com.example.jk_picker.modules.JkCameraPreview

import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

object JkCameraFactory : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        val composeView = ComposeView(context).apply {
            setContent {
                JkCameraView()
            }
        }
        return object : PlatformView {
            override fun getView(): View = composeView

            override fun dispose() {
            }

        }
    }
}