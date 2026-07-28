package com.example.ui.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat

class CameraPermissionState(
    private val context: Context,
    private val onRequestPermission: () -> Unit
) {
    val isGranted: Boolean
        get() = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    fun launchPermissionRequest() {
        if (isGranted) {
            onRequestPermission()
        } else {
            onRequestPermission()
        }
    }
}

@Composable
fun rememberCameraPermissionLauncher(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit = {}
): () -> Unit {
    val context = androidx.compose.ui.platform.LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            Toast.makeText(context, "Camera permission required to take food photos", Toast.LENGTH_SHORT).show()
            onPermissionDenied()
        }
    }

    return remember(context) {
        {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                onPermissionGranted()
            } else {
                launcher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}

@Composable
fun rememberAudioPermissionLauncher(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit = {}
): () -> Unit {
    val context = androidx.compose.ui.platform.LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            Toast.makeText(context, "Microphone permission required for voice meal input", Toast.LENGTH_SHORT).show()
            onPermissionDenied()
        }
    }

    return remember(context) {
        {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                onPermissionGranted()
            } else {
                launcher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
}
