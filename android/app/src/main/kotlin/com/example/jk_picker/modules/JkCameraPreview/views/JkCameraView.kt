package com.example.jk_picker.modules.JkCameraPreview

import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.jk_picker.modules.JkCameraPreview.viewModel.CameraOption
import com.example.jk_picker.modules.JkCameraPreview.viewModel.CameraUiState
import com.example.jk_picker.modules.JkCameraPreview.viewModel.CameraViewModel

@Composable
fun CameraContent(
    previewView: PreviewView,
    viewModel: CameraViewModel,
    onTakePhoto: () -> Unit,
    onSwitch: () -> Unit,
    onPreview: ()->Unit
) {
    val captureBitmap by viewModel.capturedImage
    val cameraOption by viewModel.cameraOption
    val isRecording by viewModel.isRecording
    val recordingTime by viewModel.recordingTime

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Camera Preview
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Red),
            factory = { previewView }
        )

        Box(
            modifier = Modifier
                .background(Color.Black)
                .fillMaxWidth()
                .height(150.dp)
                .align(Alignment.BottomCenter)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = { viewModel.setCameraOption(CameraOption.PHOTO) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Photo",
                            tint = if (cameraOption == CameraOption.PHOTO) Color.White else Color.DarkGray
                        )
                    }
                    IconButton(
                        onClick = { viewModel.setCameraOption(CameraOption.VIDEO) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video",
                            tint = if (cameraOption == CameraOption.VIDEO) Color.White else Color.DarkGray
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 20.dp)
                ) {
                    captureBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(60.dp)
                                .align(Alignment.CenterStart)
                                .clickable(onClick = {onPreview()}),
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
                        onClick = { onTakePhoto() }
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color.Black)
                ) {
                    IconButton(
                        modifier = Modifier.align(Alignment.Center),
                        onClick = { onSwitch() },
                        enabled = true,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch Camera",
                            tint = Color.White
                        )
                    }
                }
            }
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



