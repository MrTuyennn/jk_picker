package com.example.jk_picker.modules.JkCameraPreview

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class JkCameraFactory(private val activity: Activity, private val lifecycleOwner: LifecycleOwner) :
    PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {

        return JkCameraWidget(activity, context, lifecycleOwner)



    }


}

