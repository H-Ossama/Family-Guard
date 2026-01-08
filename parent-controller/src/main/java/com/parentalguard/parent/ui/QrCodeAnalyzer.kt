package com.parentalguard.parent.ui

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader()

    override fun analyze(image: ImageProxy) {
        if (image.format == ImageFormat.YUV_420_888 || image.format == ImageFormat.YUV_422_888 || image.format == ImageFormat.YUV_444_888) {
            val byteBuffer = image.planes[0].buffer
            val data = ByteArray(byteBuffer.remaining())
            byteBuffer.get(data)

            val width = image.width
            val height = image.height
            
            // Handle rotation if needed (simple approximation, ideally rotate YUV)
            // For now, let's just use the raw data but create a larger source to handle potential crop/rotation issues?
            // Actually, ZXing often fails if orientation is wrong.
            // Let's try to just pass it as is first, effectively verifying safe closure usage which was missing in some paths.
            // If user says "not scanning", rotation is likely the key.
            // Let's assume portrait mode (90 deg rotation usually needed).
            
            // NOTE: A proper YUV rotation is expensive in Java/Kotlin. 
            // We'll rely on MLKit ideally, but sticking to ZXing as requested/existing.
            // We can try to decode; if fail, we might want to try rotated?
            // For simplicity and performance, let's stick to standard decoding but ensure we CLOSE THE IMAGE.
            
            val source = PlanarYUVLuminanceSource(
                data,
                image.planes[0].rowStride,
                height,
                0,
                0,
                width,
                height,
                false
            )
            
            // Try standard decode
            var binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            try {
                val result = reader.decode(binaryBitmap)
                onQrCodeScanned(result.text)
                image.close()
                return
            } catch (e: Exception) {
                // Failed, try rotating 90 degrees (common for portrait mode)
            }
            
            // Rotate 90 degrees
            // Note: PlanarYUVLuminanceSource.rotateCounterClockwise() returns a new source
            // We might need to rotate based on image.imageInfo.rotationDegrees if valid
            // But usually brute forcing 90 degree rotation steps is robust enough for scanning
            
            try {
                // Rotate 90
                val rotatedSource = source.rotateCounterClockwise()
                binaryBitmap = BinaryBitmap(HybridBinarizer(rotatedSource))
                val result = reader.decode(binaryBitmap)
                onQrCodeScanned(result.text)
            } catch (e: Exception) {
                // Still failed
            } finally {
                image.close()
            }
        } else {
            image.close()
        }
    }
}
