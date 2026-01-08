package com.parentalguard.child.ui.screens

import android.graphics.Bitmap
import androidx.compose.ui.res.stringResource
import com.parentalguard.child.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parentalguard.child.ui.components.*
import com.parentalguard.child.ui.theme.*

@Composable
fun MainScreen(
    connectionString: String,
    status: String,
    qrBitmap: Bitmap?,
    onRequestUnlock: () -> Unit,
    onHideIcon: () -> Unit
) {
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            // Header Logic / Status
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.child_agent_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isConnected = !status.contains("Unavailable", true) && !status.contains("No Network", true)
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(if(isConnected) OnlineGreen else OfflineRed, androidx.compose.foundation.shape.CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = connectionString,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // QR Code
            GlassCard(
                modifier = Modifier
                    .aspectRatio(1f) // Square
                    .fillMaxWidth(),
                elevation = 0.dp,
                backgroundColor = Color.White.copy(alpha = 0.9f) // Light background for QR readability
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        CircularProgressIndicator(color = Primary)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            GradientButton(
                text = stringResource(R.string.request_device_unlock), // Request Unlock
                onClick = onRequestUnlock,
                icon = Icons.Default.LockOpen,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Hide Icon Button (Subtle)
            TextButton(onClick = onHideIcon) {
                Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = Color.White.copy(alpha=0.5f))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.hide_icon_short), color = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}
