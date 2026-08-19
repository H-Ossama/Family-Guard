package com.parentalguard.child.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.parentalguard.child.data.RuleRepository
import com.parentalguard.child.R
import com.parentalguard.common.model.BlockingScreenStyle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BlockingActivity : AppCompatActivity() {
    private var blockedPackageName: String = "Unknown"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        blockedPackageName = intent.getStringExtra("PACKAGE_NAME") ?: "Unknown"
        renderStyle(RuleRepository.blockingScreenStyle.value)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                RuleRepository.blockingScreenStyle.collectLatest { style ->
                    renderStyle(style)
                }
            }
        }
    }

    private fun renderStyle(style: BlockingScreenStyle) {
        when (style) {
            BlockingScreenStyle.CURRENT -> {
                setContentView(R.layout.activity_blocking)
                findViewById<TextView>(R.id.tv_blocked_msg).text = getString(R.string.blocking_restricted_msg, blockedPackageName)
            }
            BlockingScreenStyle.BLACKOUT -> setContentView(
                FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
            )
            BlockingScreenStyle.QUIET_FOCUS -> setContentView(createQuietFocusView(blockedPackageName))
        }
    }

    private fun createQuietFocusView(packageName: String): View {
        val padding = (32 * resources.displayMetrics.density).toInt()
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(padding, padding, padding, padding)
            setBackgroundColor(Color.rgb(17, 25, 54))

            addView(TextView(context).apply {
                text = getString(R.string.quiet_focus_title)
                setTextColor(Color.WHITE)
                textSize = 28f
                gravity = Gravity.CENTER
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }, LinearLayout.LayoutParams(-1, -2))

            addView(TextView(context).apply {
                text = getString(R.string.quiet_focus_message)
                setTextColor(Color.WHITE)
                alpha = 0.78f
                textSize = 17f
                gravity = Gravity.CENTER
                setPadding(0, padding / 2, 0, padding / 3)
            }, LinearLayout.LayoutParams(-1, -2))

            addView(TextView(context).apply {
                text = getString(R.string.blocking_restricted_msg, packageName)
                setTextColor(Color.rgb(98, 230, 214))
                textSize = 14f
                gravity = Gravity.CENTER
            }, LinearLayout.LayoutParams(-1, -2))
        }
    }

    override fun onBackPressed() {
        // Go straight to home, do not allow backing into the app
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}
