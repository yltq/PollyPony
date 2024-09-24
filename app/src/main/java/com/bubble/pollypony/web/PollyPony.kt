package com.bubble.pollypony.web

import android.app.Application
import com.bubble.pollypony.start.ShopFrontActivity
import com.bubble.pollypony.start.ViewParams.Companion.ponyMain
import com.github.shadowsocks.Core
import kotlin.properties.Delegates

class PollyPony: Application() {
    companion object {
        var pony: PollyPony by Delegates.notNull()
        var showPonyCold: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        pony = this
        val ponyMain = ponyMain()
        Core.init(this, ShopFrontActivity::class)
        if (ponyMain) {
            showPonyCold = true
        }
    }
}