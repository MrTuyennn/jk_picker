package com.example.jk_picker

import com.example.jk_picker.modules.JkCameraPreview.JkCameraFactory
import com.example.jk_picker.modules.JkImagePicker.JkImagePicker
import com.example.jk_picker.utils.AppConstant
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity : FlutterFragmentActivity() {
    var permissionResult: ((requestCode: Int, permissions: Array<out String>, grantResults: IntArray) -> Unit)? = null

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionResult?.invoke(requestCode, permissions, grantResults)
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        JkImagePicker.initMethodChannel(flutterEngine, this)
        flutterEngine.platformViewsController.registry.registerViewFactory(
            AppConstant.CHANNEL_JK_CAMERA,
            JkCameraFactory(this, this)
        )
    }

}
