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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size as CmSize
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.parentalguard.parent.R
import com.parentalguard.parent.ui.neumorphic.Nm
import com.parentalguard.parent.ui.neumorphic.NeumorphicBackground
import com.parentalguard.parent.ui.neumorphic.NeumorphicEmptyState
import com.parentalguard.parent.ui.neumorphic.NeumorphicIconTile
import com.parentalguard.parent.ui.neumorphic.neumorphic
import java.util.concurrent.Executors

@Composable
fun QRScannerScreen(
    onQrScanned: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCamPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCamPermission = granted
            if (!granted) onBack()
        }
    )

    LaunchedEffect(true) {
        if (!hasCamPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    var isScanned by remember { mutableStateOf(false) }

    if (!hasCamPermission) {
        NeumorphicBackground {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                NeumorphicEmptyState(
                    icon = Icons.Default.QrCodeScanner,
                    title = stringResource(R.string.camera_permission_required),
                    description = ""
                )
            }
        }
        return
    }

    NeumorphicBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NeumorphicIconTile(
                    icon = Icons.Default.ArrowBack,
                    onClick = onBack,
                    tint = Nm.onSurface,
                    contentDescription = stringResource(R.string.back),
                    size = 46.dp
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    text = stringResource(R.string.scan_qr),
                    color = Nm.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .neumorphic(
                        shape = RoundedCornerShape(36.dp),
                        backgroundColor = Color.Black,
                        elevation = 10.dp
                    )
                    .clip(RoundedCornerShape(36.dp))
            ) {
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
                                .setTargetResolution(Size(1280, 720))
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(
                                Executors.newSingleThreadExecutor(),
                                QrCodeAnalyzer { result ->
                                    val points = result.resultPoints
                                    if (!points.isNullOrEmpty() && !isScanned) {
                                        isScanned = true
                                        previewView.post { onQrScanned(result.text) }
                                    }
                                }
                            )

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
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

                ScannerOverlay(isScanned = isScanned)

                if (isScanned) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Nm.success
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .neumorphic(shape = RoundedCornerShape(50), backgroundColor = Nm.surface, elevation = 5.dp)
                    .clip(RoundedCornerShape(50))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.scan_instruction),
                    color = Nm.onSurface,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ScannerOverlay(isScanned: Boolean) {
    val laser by rememberInfiniteTransition(label = "laser").animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Reverse),
        label = "laser-y"
    )
    val accent = if (isScanned) Nm.success else Nm.primary

    Canvas(modifier = Modifier.fillMaxSize()) {
        val boxSize = size.width * 0.68f
        val left = (size.width - boxSize) / 2f
        val top = (size.height - boxSize) / 2f
        val radius = 32.dp.toPx()

        // Dimmed scrim with a rounded window
        val scrim = Path().apply {
            addRect(Rect(0f, 0f, size.width, size.height))
            addRoundRect(RoundRect(left, top, left + boxSize, top + boxSize, radius, radius))
            fillType = PathFillType.EvenOdd
        }
        drawPath(scrim, Color.Black.copy(alpha = 0.5f))

        // Recessed groove frame: dark inner shadow + light top edge
        drawRoundRect(
            color = Nm.darkShadow.copy(alpha = 0.6f),
            topLeft = Offset(left + 2.dp.toPx(), top + 2.dp.toPx()),
            size = CmSize(boxSize - 4.dp.toPx(), boxSize - 4.dp.toPx()),
            cornerRadius = CornerRadius(radius - 2.dp.toPx()),
            style = Stroke(width = 4.dp.toPx())
        )
        drawRoundRect(
            color = Nm.lightShadow.copy(alpha = 0.85f),
            topLeft = Offset(left - 2.dp.toPx(), top - 2.dp.toPx()),
            size = CmSize(boxSize + 4.dp.toPx(), boxSize + 4.dp.toPx()),
            cornerRadius = CornerRadius(radius + 2.dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )

        // Scan line
        if (!isScanned) {
            val laserY = top + 14.dp.toPx() + ((boxSize - 28.dp.toPx()) * laser)
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, accent.copy(alpha = 0.9f), Color.Transparent)
                ),
                start = Offset(left + 16.dp.toPx(), laserY),
                end = Offset(left + boxSize - 16.dp.toPx(), laserY),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}