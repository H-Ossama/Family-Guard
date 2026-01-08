package com.parentalguard.child.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.parentalguard.child.R

class BlockingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure this activity takes over full screen and cannot be easily dismissed
        setContentView(R.layout.activity_blocking)
        
        val pkg = intent.getStringExtra("PACKAGE_NAME") ?: "Unknown"
        findViewById<TextView>(R.id.tv_blocked_msg).text = getString(R.string.blocking_restricted_msg, pkg)
    }

    override fun onBackPressed() {
        // Go straight to home, do not allow backing into the app
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}
