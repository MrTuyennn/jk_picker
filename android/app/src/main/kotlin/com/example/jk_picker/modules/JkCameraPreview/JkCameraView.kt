package com.example.jk_picker.modules.JkCameraPreview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.Image
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.Window
import android.widget.LinearLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.impl.ImageOutputConfig.RotationDegreesValue
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jk_picker.modules.JkCameraPreview.CameraModel.CameraState
import com.example.jk_picker.modules.JkCameraPreview.CameraModel.CameraViewModel
import com.google.common.util.concurrent.ListenableFuture
import org.koin.androidx.compose.koinViewModel
import java.util.concurrent.Future


@Composable
fun JkCameraView(
    viewModel: CameraViewModel = koinViewModel()
) {
    val cameraState:CameraState by viewModel.state.collectAsState()
    CameraContent(
        onPhotoCaptured = viewModel:: storePhotoInGallery,
        lastCapturedPhoto = cameraState.capturedImage
    )
}

@Composable
private fun CameraContent(
    onPhotoCaptured: (Bitmap) -> Unit,
    lastCapturedPhoto: Bitmap? = null

) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = ({
            ExtendedFloatingActionButton(
                text = { Text(text = "Take photo") },
                onClick = { capturePhoto(context, cameraController, onPhotoCaptured) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Camera capture icon"
                    )
                }
            )
        })
    ) { paddingValues: PaddingValues ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            factory = {
                PreviewView(it).apply {
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    setBackgroundColor(android.graphics.Color.WHITE)
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FIT_CENTER
                }.also { previewView: PreviewView ->
                    previewView.controller = cameraController
                    cameraController.bindToLifecycle(lifecycleOwner)

                }
            }
        )

        if(lastCapturedPhoto!=null){
            LastPhotoPreview(
                lastCapturedPhoto = lastCapturedPhoto

            )
        }
    }

    // Sử dụng DisposableEffect để đảm bảo camera được hủy liên kết khi rời khỏi màn hình
    DisposableEffect(lifecycleOwner) {
        onDispose {
            cameraController.unbind()
        }
    }
}

@Composable
fun PermissionDeniedScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Ứng dụng cần quyền camera để hoạt động.",
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
        Button(onClick = onRequestPermission) {
            Text("Yêu cầu lại quyền")
        }
    }
}

fun capturePhoto(context: Context, cameraController: LifecycleCameraController, onPhotoCaptured: (Bitmap) -> Unit) {
    val mainExecutor = ContextCompat.getMainExecutor(context)
    cameraController.takePicture(mainExecutor, object : ImageCapture.OnImageCapturedCallback(){
        override fun onCaptureSuccess(image: ImageProxy) {
            super.onCaptureSuccess(image)
            val correctedBitmap:Bitmap = image.toBitmap().rotateBitmap(image.imageInfo.rotationDegrees)
            onPhotoCaptured(correctedBitmap)
            image.close()
        }

        override fun onError(exception: ImageCaptureException) {
            super.onError(exception)
            Log.e("CameraContent","Error capturing camera", exception)
        }
    })
}

@Composable
private fun LastPhotoPreview(
    lastCapturedPhoto: Bitmap
) {

    val capturedPhoto: ImageBitmap = remember(lastCapturedPhoto.hashCode()) { lastCapturedPhoto.asImageBitmap() }

    Box (
        modifier = Modifier.fillMaxSize()

    ){
        Card(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(128.dp)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Image(
                bitmap = capturedPhoto,
                contentDescription = "Last captured photo",
                contentScale = ContentScale.Crop
            )
        }
    }
}

fun Bitmap.rotateBitmap(degrees: Int): Bitmap {
    val matrix = Matrix().apply {
        postRotate(-degrees.toFloat())
        postScale(-1f, -1f)
    }
    return Bitmap.createBitmap(this, 0,0,width,height, matrix, false)
}