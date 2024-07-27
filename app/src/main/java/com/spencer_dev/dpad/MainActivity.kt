package com.spencer_dev.dpad

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btn_interval_refresh).setOnClickListener {
            startActivity(Intent(this, IntervalRefreshActivity::class.java))
        }
    }
}
