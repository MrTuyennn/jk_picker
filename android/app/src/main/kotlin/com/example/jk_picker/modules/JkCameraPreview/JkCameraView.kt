package com.example.jk_picker.modules.JkCameraPreview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp


@Composable
fun JkCameraView() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            Text(
                text = "Nguyen Nghien Trinh",
                fontSize = 16.sp,
                color = Color.Black,
                softWrap = true,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Button(
                onClick = { /* TODO */ },
                enabled = true
            ) {
                Text("On press")
            }
        }
    }
}
