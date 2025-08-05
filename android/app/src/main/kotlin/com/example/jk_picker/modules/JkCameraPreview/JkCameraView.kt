package com.example.jk_picker.modules.JkCameraPreview

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.rememberAsyncImagePainter
import com.example.jk_picker.NavigationRoutes
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun JkCameraView() {
    Surface(modifier = Modifier.fillMaxSize()) {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = NavigationRoutes.Camera.name) {
            composable(NavigationRoutes.Camera.name) {
                CameraContent(onPhotoCaptured = {
                    val encodedUri = Uri.encode(it.toString())
                    navController.navigate("${NavigationRoutes.Preview.name}?uri=$encodedUri")
                })
            }
            composable(
                "${NavigationRoutes.Preview.name}?uri={uri}",
                arguments = listOf(navArgument("uri") {
                    type =
                        NavType.StringType
                })
            ) {

                val uriString = it.arguments?.getString("uri")
                val uri = uriString?.let { Uri.parse(uriString) }

                if(uri!=null){
                    JkPreviewScreen(
                        photoUri = uri,
                        onBack = {navController.popBackStack()})
                }
            }
        }

    }
}

@Composable
private fun CameraContent(onPhotoCaptured:(Uri)->Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context)
    }
    var capturedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }


    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // 1. Camera Preview
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Red) // nền đen cho toàn bộ
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red),
                factory = {
                    previewView.scaleType = PreviewView.ScaleType.FIT_CENTER
                    previewView
                }
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.Black)
                .fillMaxWidth()
                .height(100.dp), // hoặc chiều cao bạn muốn
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight().padding(horizontal = 20.dp)
            ) {
                capturedImageUri?.let { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .align(Alignment.CenterStart).clickable(onClick = {onPhotoCaptured(uri)}),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                CaptureButton(
                    modifier = Modifier.align(Alignment.Center),
                    onClick = {
                        takePhoto(context, imageCapture!!) { uri ->
                            capturedImageUri = uri
                        }
                    }
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color.Black)
            )
        }


    }




    LaunchedEffect(previewView) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).await()
        val preview = Preview.Builder().build().apply {
            surfaceProvider = previewView.surfaceProvider
        }
        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        val rotation = previewView.display?.rotation ?: Surface.ROTATION_0
        val newImageCapture = ImageCapture.Builder()
            .setTargetRotation(rotation)
            .build()

        imageCapture = newImageCapture // ✅ set đúng instance đã bind

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, newImageCapture)
        } catch (e: Exception) {
            e.printStackTrace()
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

@Composable
fun CaptureButton(
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(4.dp, Color.LightGray, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.Red)
        )
    }
}


fun takePhoto(context: Context, imageCapture: ImageCapture, onImageCapture: (Uri) -> Unit) {
    val name =
        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
        }
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                Log.e("Error", "Photo capture failed: ${exception.message}", exception)
            }

            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = outputFileResults.savedUri
                onImageCapture(savedUri!!)
            }
        })
}