package com.bubble.pollypony.web

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import com.bubble.pollypony.R

class PonyWebActivity : AppCompatActivity() {
    companion object {
        const val PRIVACY_POLICY = "https://www.google.com/"
    }

    private lateinit var c_light_back: ImageView
    private lateinit var pony_web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pony_web)
        c_light_back = findViewById(R.id.c_light_back)
        pony_web = findViewById(R.id.pony_web)

        pony_web.webViewClient = WebViewClient()
        val webSettings: WebSettings = pony_web.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        pony_web.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
            }

        }
        pony_web.loadUrl(PRIVACY_POLICY)
        c_light_back.setOnClickListener {
            finish()
        }
    }
}