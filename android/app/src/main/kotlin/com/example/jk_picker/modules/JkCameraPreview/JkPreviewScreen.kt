package com.example.jk_picker.modules.JkCameraPreview

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun JkPreviewScreen(photoUri: Uri, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = rememberAsyncImagePainter(photoUri),
            contentDescription = "Captured photo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        // Nút back đơn giản
        Text(
            "Quay lại",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onBack() }
                .padding(12.dp),
            color = Color.White
        )
    }
}
