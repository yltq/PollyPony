package com.bubble.pollypony.start

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import com.bubble.pollypony.R
import com.bubble.pollypony.web.PonyWebActivity

class AccountActivity : AppCompatActivity() {
    private lateinit var c_light_back: ImageView
    private lateinit var account_s: LinearLayoutCompat
    private lateinit var account_p: LinearLayoutCompat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        c_light_back = findViewById(R.id.c_light_back)
        account_s = findViewById(R.id.account_s)
        account_p = findViewById(R.id.account_p)

        c_light_back.setOnClickListener {
            finish()
        }
        account_s.setOnClickListener {
            toShare("https://play.google.com/store/apps/details?id=${packageName}")
        }
        account_p.setOnClickListener {
            toPolicy()
        }
    }

    private fun toPolicy() {
        startActivity(Intent(this, PonyWebActivity::class.java))
        finish()
    }

    private fun toShare(msg: String) {
        Intent().also {
            it.action = Intent.ACTION_SEND
            it.putExtra(Intent.EXTRA_TEXT, msg)
            it.type = "text/plain"
            startActivity(Intent.createChooser(it, "Share app"))
        }
    }
}