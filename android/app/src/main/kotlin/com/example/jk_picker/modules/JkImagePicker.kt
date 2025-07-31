package com.example.jk_picker.modules

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.jk_picker.utils.AppConstant
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class JkImagePicker() {
    fun initMethodChannel(flutterEngine: FlutterEngine, context: Context){
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, AppConstant.CHANNEL_JK_PICKER).setMethodCallHandler { call: MethodCall, result: MethodChannel.Result ->
            when (call.method){
                AppConstant.METHOD_GET_ALBUMS ->{}
                AppConstant.METHOD_GET_IMAGE ->{}
                AppConstant.METHOD_GET_ASSETSINALBUM ->{}
                else ->{
                    result.notImplemented()
                }
            }
        }
    }
}