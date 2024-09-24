package com.bubble.pollypony.start

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.lifecycleScope
import com.bubble.pollypony.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InspectActivity : AppCompatActivity() {
    private var download: String = "0kb/s"
    private var upload: String = "0kb/s"
    private var speed: String = "-ms"

    private lateinit var c_light_back: ImageView
    private lateinit var inspect_down: TextView
    private lateinit var inspect_up: TextView
    private lateinit var inspect_ms: TextView
    private lateinit var retry: LinearLayoutCompat
    private lateinit var down_loading: ProgressBar
    private lateinit var up_loading: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inspect)
        c_light_back = findViewById(R.id.c_light_back)
        inspect_down = findViewById(R.id.inspect_down)
        inspect_up = findViewById(R.id.inspect_up)
        inspect_ms = findViewById(R.id.inspect_ms)
        retry = findViewById(R.id.retry)
        down_loading = findViewById(R.id.down_loading)
        up_loading = findViewById(R.id.up_loading)


        val extra = intent.extras
        extra?.apply {
            download = getString("download")?:"0kb/s"
            upload = getString("upload")?:"0kb/s"
            speed = getString("ping")?:"-ms"
        }
        if (download.isEmpty()) {
            download = "0kb/s"
        }
        if (upload.isEmpty()) {
            upload = "0kb/s"
        }
        if (speed.isEmpty()) {
            speed = "-ms"
        }

        inspect_down.text = download
        inspect_up.text = upload
        inspect_ms.text = speed
        c_light_back.setOnClickListener {
            finish()
        }
        retry.setOnClickListener {
            PonySpeedParams.endAllPingSpeed()
            down_loading.visibility = View.VISIBLE
            up_loading.visibility = View.VISIBLE
            inspect_down.visibility = View.GONE
            inspect_up.visibility = View.GONE
            PonySpeedParams.ponyDownload(download = {
                lifecycleScope.launch(Dispatchers.Main) {
                    inspect_down.visibility = View.VISIBLE
                    inspect_down.text = it
                    down_loading.visibility = View.GONE
                }
            }, upload = {
                lifecycleScope.launch(Dispatchers.Main) {
                    inspect_up.visibility = View.VISIBLE
                    inspect_up.text = it
                    up_loading.visibility = View.GONE
                }
            })
            PonySpeedParams.ponyPing {
                lifecycleScope.launch(Dispatchers.Main) {
                    inspect_ms.text = it
                }
            }
        }
    }
}