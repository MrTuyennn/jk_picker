package com.example.jk_picker

import com.example.jk_picker.modules.JkImagePicker
import com.example.jk_picker.utils.AppConstant
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity: FlutterActivity() {
    private val AppChannel = AppConstant.CHANNEL_JK_PICKER

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        JkImagePicker().initMethodChannel(flutterEngine, this);

    }
}
