package com.example.jk_picker.modules.JkCameraPreview.viewModel

import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


enum class NavigationRoutes(){
    CameraPreview,
    PhotoPreview,
    ListPhoto,
}

// Define a proper enum class for the camera UI states.
enum class CameraUiState {
    PREVIEW,
    PHOTO_CAPTURED,
    LIST_PHOTO,
}

enum class CameraFacing {
    BACK,
    FRONT,
}

enum class CameraOption {
    PHOTO,
    VIDEO,
}


class CameraViewModel: ViewModel() {
    // init capturedImage state
    private val _capturedImage = mutableStateOf<Bitmap?>(null)
    val capturedImage: State<Bitmap?> = _capturedImage
    // init capturedImage state

    // init uiState state
    private val _uiState = mutableStateOf(CameraUiState.PHOTO_CAPTURED)
    val uiState: State<CameraUiState> = _uiState
    // init uiState state

    //init cameraFacing state
    private val _cameraFacing = mutableStateOf(CameraFacing.BACK)
    val cameraFacing: State<CameraFacing> = _cameraFacing
    //init cameraFacing state

    //init cameraOption state
    private val _cameraOption = mutableStateOf(CameraOption.PHOTO)
    val cameraOption: State<CameraOption> = _cameraOption
    //init cameraOption state
    private val _isRecording = mutableStateOf(false)
    val isRecording: State<Boolean> = _isRecording

    private val _recordingTime = mutableIntStateOf(0)
    val recordingTime: State<Int> = _recordingTime

    private var timerJob: Job? = null

    fun startRecording() {
        _isRecording.value = true
        _recordingTime.intValue = 0
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L) // Wait for 1 second
                _recordingTime.intValue++
            }
        }
    }

    fun stopRecording() {
        _isRecording.value = false
        timerJob?.cancel() // Stop the timer coroutine
        _recordingTime.intValue = 0
    }
    fun setRecording(isRecording: Boolean) {
        _isRecording.value = isRecording
    }
    fun setCameraFacing(facing: CameraFacing) {
        _cameraFacing.value = facing
    }

    fun setCameraOption(option: CameraOption) {
        _cameraOption.value = option
    }

    fun setImage(image: Bitmap) {
        _capturedImage.value = image
    }

    fun setUIState(state: CameraUiState) {
        _uiState.value = state
    }
    /**
     * Resets the captured image and updates the UI state back to PREVIEW.
     */
    fun clearImage() {
        _capturedImage.value = null
        _uiState.value = CameraUiState.PREVIEW
    }
}

class CameraPreview(){

}