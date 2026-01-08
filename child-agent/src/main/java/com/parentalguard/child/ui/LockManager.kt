package com.parentalguard.child.ui

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.parentalguard.child.ui.screens.LockScreen
import com.parentalguard.child.ui.theme.ParentalGuardTheme
import android.widget.Toast

class LockManager(private val context: Context) : LifecycleOwner, SavedStateRegistryOwner {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: ComposeView? = null
    val isShowing: Boolean get() = overlayView != null
    
    // Lifecycle components for Compose
    private val _lifecycleRegistry = LifecycleRegistry(this)
    private val _savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = _lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = _savedStateRegistryController.savedStateRegistry

    init {
        // Initialize SavedStateRegistry
        _savedStateRegistryController.performAttach()
        _savedStateRegistryController.performRestore(null)
    }

    fun showLockScreen() {
        if (overlayView != null) return // Already shown

        try {
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT
            )

            // Create ComposeView
            overlayView = ComposeView(context).apply {
                // Determine lifecycle owners
                setViewTreeLifecycleOwner(this@LockManager)
                setViewTreeSavedStateRegistryOwner(this@LockManager)
                
                setContent {
                    ParentalGuardTheme {
                        LockScreen(
                            onRequestUnlock = {
                                com.parentalguard.child.utils.EventHelper.sendUnlockRequest(
                                    context = context,
                                    requestType = "DEVICE"
                                )
                                Toast.makeText(context, "Unlock Requested", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
            
            // Add to Window
            windowManager.addView(overlayView, params)
            
            // Activate Lifecycle
            _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            
        } catch (e: Exception) {
            e.printStackTrace()
            // Cleanup on failure
            overlayView = null
        }
    }

    fun hideLockScreen() {
        if (overlayView == null) return

        try {
            // Deactivate Lifecycle
            _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            
            windowManager.removeView(overlayView)
            overlayView = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
