package com.bubble.pollypony.start

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.bubble.pollypony.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ActivateActivity : AppCompatActivity() {
    private var viewParams: ViewParams = ViewParams()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        })
        setContentView(R.layout.activity_activate)
    }

    override fun onResume() {
        super.onResume()
        PonyParams.webShort()
        viewParams.visible = true
        lifecycleScope.launch {
            repeat(10) {
                delay(200)
            }.also {
                if (viewParams.visible.not()) return@launch
                startActivity(Intent(this@ActivateActivity, ShopFrontActivity::class.java))
                finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewParams.visible = false
    }
}