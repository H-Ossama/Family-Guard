package com.parentalguard.parent.ui

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@Composable
fun QRScannerScreen(
    onQrScanned: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCamPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCamPermission = granted
            if (!granted) {
                onBack() 
            }
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCamPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    // State for detected QR
    var isScanned by remember { mutableStateOf(false) }

    if (hasCamPermission) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().apply {
                            setSurfaceProvider(previewView.surfaceProvider)
                        }

                        @Suppress("DEPRECATION")
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setTargetResolution(Size(1280, 720)) // 720p is good for QR
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(
                            Executors.newSingleThreadExecutor(),
                            QrCodeAnalyzer { result ->
                                // Update UI with detected points
                                val points = result.resultPoints
                                if (points != null && points.isNotEmpty() && !isScanned) {
                                    isScanned = true
                                    // Trigger callback on main thread
                                    previewView.post {
                                        onQrScanned(result.text)
                                    }
                                }
                            }
                        )

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Overlay with Viewfinder and Back Button
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Back Button
                androidx.compose.material3.IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                val borderColor = if (isScanned) Color.Green else Color.White

                // Viewfinder lines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = (if (isScanned) 8.dp else 4.dp).toPx()
                    val cornerLength = 40.dp.toPx()
                    val width = size.width
                    val height = size.height
                    val boxSize = width * 0.7f // 70% width square
                    val left = (width - boxSize) / 2
                    val top = (height - boxSize) / 2
                    val right = left + boxSize
                    val bottom = top + boxSize
                    
                    // Top Left
                    drawLine(borderColor, Offset(left, top), Offset(left + cornerLength, top), strokeWidth)
                    drawLine(borderColor, Offset(left, top), Offset(left, top + cornerLength), strokeWidth)

                    // Top Right
                    drawLine(borderColor, Offset(right, top), Offset(right - cornerLength, top), strokeWidth)
                    drawLine(borderColor, Offset(right, top), Offset(right, top + cornerLength), strokeWidth)

                    // Bottom Left
                    drawLine(borderColor, Offset(left, bottom), Offset(left + cornerLength, bottom), strokeWidth)
                    drawLine(borderColor, Offset(left, bottom), Offset(left, bottom - cornerLength), strokeWidth)

                    // Bottom Right
                    drawLine(borderColor, Offset(right, bottom), Offset(right - cornerLength, bottom), strokeWidth)
                    drawLine(borderColor, Offset(right, bottom), Offset(right, bottom - cornerLength), strokeWidth)
                }

                if (isScanned) {
                     androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 150.dp), // Below the box
                        color = Color.Green
                    )
                }

                Text(
                    text = stringResource(com.parentalguard.parent.R.string.scan_instruction),
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 64.dp)
                )
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(com.parentalguard.parent.R.string.camera_permission_required))
        }
    }
}
