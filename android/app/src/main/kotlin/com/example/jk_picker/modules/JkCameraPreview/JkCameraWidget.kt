package com.example.jk_picker.modules.JkCameraPreview

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import android.provider.MediaStore
import android.text.format.DateFormat
import android.util.Log
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jk_picker.MainActivity
import com.example.jk_picker.modules.JkCameraPreview.viewModel.CameraViewModel
import com.example.jk_picker.modules.JkCameraPreview.viewModel.NavigationRoutes
import com.example.jk_picker.modules.JkImagePicker.JkImagePicker
import com.example.jk_picker.modules.PermissionHandler
import io.flutter.plugin.platform.PlatformView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class JkCameraWidget(
    private val activity: MainActivity,
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) : PlatformView {
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private val previewView = PreviewView(context).apply {
        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
    }
    private val cameraView = ComposeView(context)
    private val viewModel = CameraViewModel()


    init {
        cameraView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        previewView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        if (PermissionHandler().hasCameraPermission(activity)) {
            setContent()
        } else {
            PermissionHandler().requestCameraPermission(activity)
        }

        activity.permissionResult = { requestCode, _, grantResults ->
            if (requestCode == PermissionHandler().CAMERA_PERMISSION_REQUEST_CODE) {
                val granted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                if (granted) {
                    setContent()
                }
            } else {
                Log.e("TAG", "=== Error requestCode===: $requestCode")
            }

        }
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (JkImagePicker.fetchAllImageBitmap(context).isNotEmpty()) {
            val lastLocalImage = JkImagePicker.fetchAllImageBitmap(context).first()
            viewModel.setImage(lastLocalImage)
        }
    }

    private fun setContent() {
        startCamera()
        cameraView.setViewTreeLifecycleOwner(lifecycleOwner)
        cameraView.setContent {
            val navController = rememberNavController()
            Surface(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = NavigationRoutes.CameraPreview.name
                ) {
                    composable(NavigationRoutes.CameraPreview.name) {
                        CameraContent(
                            onSwitch = { switchCamera() },
                            onTakePhoto = { takePhoto() },
                            viewModel = viewModel,
                            previewView = previewView,
                            onPreview = { navController.navigate(NavigationRoutes.ListPhoto.name) }
                        )
                    }
                    composable(route = NavigationRoutes.PhotoPreview.name) {
                        JkPreviewScreen(viewModel.capturedImage.value!!) {
                            navController.popBackStack()
                        }
                    }
                    composable(route = NavigationRoutes.ListPhoto.name) {
                        ListPhotoScreen(
                            onClick = { image ->
                                viewModel.setImage(image)
                                navController.navigate(NavigationRoutes.PhotoPreview.name)
                            }, onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val newCameraProvider = cameraProviderFuture.get()
            cameraProvider = newCameraProvider

            val surfacePreview =
                Preview.Builder().setTargetRotation(Surface.ROTATION_0).build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
            val newImageCapture =
                ImageCapture.Builder().setTargetRotation(Surface.ROTATION_0).build()
            imageCapture = newImageCapture
            val newVideoCapture = VideoCapture.withOutput(Recorder.Builder().build())
            videoCapture = newVideoCapture

            try {
                newCameraProvider.unbindAll()
                newCameraProvider.bindToLifecycle(
                    activity,
                    cameraSelector,
                    surfacePreview,
                    imageCapture,
                    videoCapture
                )
            } catch (e: Exception) {
                Log.e("CameraSwitch", "Binding failed: ${e.message}", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val name = DateFormat.getTimeFormat(context).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    onImageCaptured(image)
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Error", "Photo capture failed: ${exception.message}", exception)
                }
            }
        )

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("Error", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d("Photo captured", "success")
                }
            }
        )
    }

    // Biến trạng thái camera hiện tại
    private var currentCameraFacing = CameraSelector.LENS_FACING_BACK
    fun switchCamera() {
        currentCameraFacing = if (currentCameraFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        cameraSelector = CameraSelector.Builder().requireLensFacing(currentCameraFacing).build()
        val surfacePreview = Preview.Builder().setTargetRotation(Surface.ROTATION_0).build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }
        cameraProvider!!.unbindAll()
        cameraProvider!!.bindToLifecycle(
            activity,
            cameraSelector,
            surfacePreview,
            imageCapture,
            videoCapture
        )
    }

    private fun onImageCaptured(image: ImageProxy) {
        val bitmap = image.toBitmap()
        val rotationDegrees = image.imageInfo.rotationDegrees

        val matrix = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
        }

        // Tạo một Bitmap mới đã được xoay đúng hướng
        val rotatedBitmap = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
        viewModel.setImage(rotatedBitmap)
        image.close()
    }

    override fun getView(): View = cameraView

    override fun dispose() {
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
    }
}