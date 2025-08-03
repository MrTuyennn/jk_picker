package com.example.jk_picker

import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LifecycleOwner
import com.example.jk_picker.modules.JkCameraPreview.CameraModel.CameraViewModel
import com.example.jk_picker.modules.JkCameraPreview.CameraModel.SavePhotoToGalleryUseCase
import com.example.jk_picker.modules.JkCameraPreview.JkCameraFactory
import com.example.jk_picker.modules.JkImagePicker
import com.example.jk_picker.modules.PermissionHandler
import com.example.jk_picker.utils.AppConstant
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MainActivity: FlutterFragmentActivity() {

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        startKoin {
            androidLogger()
            androidContext(this@MainActivity)
           modules(
               module {
                   viewModel { CameraViewModel(get()) }
                   single {  SavePhotoToGalleryUseCase(get()) }

               }
           )

        }
        JkImagePicker.initMethodChannel(flutterEngine, this)
        flutterEngine.platformViewsController.registry.registerViewFactory(AppConstant.CHANNEL_JK_CAMERA, JkCameraFactory(this,this))
    }
}
