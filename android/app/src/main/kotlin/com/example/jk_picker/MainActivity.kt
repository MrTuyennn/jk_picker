package com.example.jk_picker

import com.example.jk_picker.modules.JkImagePicker
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity: FlutterActivity() {

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        JkImagePicker.initMethodChannel(flutterEngine, this);

    }
}
