package com.bubble.pollypony.start

import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bubble.pollypony.R
import com.bubble.pollypony.mut.ChildTopAdapter
import com.bubble.pollypony.start.PonyParams.Companion.showPonyDialog
import com.bubble.pollypony.web.PollyPony
import com.bubble.pollypony.web.PonyWebFace
import com.github.shadowsocks.Core
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.database.Profile
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.DataStore
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class PonyParams {
    companion object {
        var inLimited: Boolean = arrayListOf("CN", "IR", "HK", "MO").count { it == Locale.getDefault().country } > 0
        fun webShort() {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("https://api.infoip.io")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val face: PonyWebFace = retrofit.create(PonyWebFace::class.java)
            face.webShort(WebView(PollyPony.pony).settings.userAgentString).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val body = response.body() ?: return
                        kotlin.runCatching {
                            val json = JSONObject(body.string())
                            val code = json.optString("country_short")
                            inLimited = arrayListOf("CN", "IR", "HK", "MO").count { it == code } > 0
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    t.printStackTrace()
                }

            })
        }
        fun webEnable(context: Context, call: (Boolean) -> Unit) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCapabilities = connectivityManager.activeNetwork?.let {
                connectivityManager.getNetworkCapabilities(it)
            }
            val enable = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            call.invoke(enable)
        }
        var autoList: MutableList<ChildTopAdapter.ChildTopContent> =
            mutableListOf<ChildTopAdapter.ChildTopContent>().run {
                add(ChildTopAdapter.ChildTopContent().apply {
                    country = "United States"
                    city = "c1"
                    ip = "104.160.42.226"
                    port = 15909
                    which = "aes-256-gcm"
                    word = "JtQbdQ5SvsmraSDz"
                    atAuto = true
                })
                add(ChildTopAdapter.ChildTopContent().apply {
                    country = "United States"
                    city = "c2"
                    ip = "104.160.42.226"
                    port = 15909
                    which = "aes-256-gcm"
                    word = "JtQbdQ5SvsmraSDz"
                    atAuto = true
                })
                add(ChildTopAdapter.ChildTopContent().apply {
                    country = "United States"
                    city = "c3"
                    ip = "104.160.42.226"
                    port = 15909
                    which = "aes-256-gcm"
                    word = "JtQbdQ5SvsmraSDz"
                    atAuto = true
                })
                this
            }
        var notAutoList: MutableList<ChildTopAdapter.ChildTopContent> =
            mutableListOf<ChildTopAdapter.ChildTopContent>().run {
                add(ChildTopAdapter.ChildTopContent().apply {
                    country = "United States"
                    city = "c1"
                    ip = "104.160.42.243"
                    port = 15909
                    which = "aes-256-gcm"
                    word = "JtQbdQ5SvsmraSDz"
                    atAuto = false
                })
                add(ChildTopAdapter.ChildTopContent().apply {
                    country = "United States"
                    city = "c2"
                    ip = "104.160.42.226"
                    port = 15909
                    which = "aes-256-gcm"
                    word = "JtQbdQ5SvsmraSDz"
                    atAuto = false
                })
                add(ChildTopAdapter.ChildTopContent().apply {
                    country = "United States"
                    city = "c3"
                    ip = "104.160.42.226"
                    port = 15909
                    which = "aes-256-gcm"
                    word = "JtQbdQ5SvsmraSDz"
                    atAuto = false
                })
                add(ChildTopAdapter.ChildTopContent().apply {
                    country = "Japan"
                    city = "c4"
                    ip = "104.160.42.226"
                    port = 15909
                    which = "aes-256-gcm"
                    word = "JtQbdQ5SvsmraSDz"
                    atAuto = false
                })
                add(ChildTopAdapter.ChildTopContent().apply {
                    country = "Belgium"
                    city = "c5"
                    ip = "104.160.42.226"
                    port = 15909
                    which = "aes-256-gcm"
                    word = "JtQbdQ5SvsmraSDz"
                    atAuto = false
                })
                this
            }
        var ponyConnected: Boolean = false
        var ponyStatus: BaseService.State = BaseService.State.Stopped
        var lastContent: ChildTopAdapter.ChildTopContent? = null

        fun ponyStatusIndex(): Int {
            return arrayListOf<BaseService.State>(BaseService.State.Stopped, BaseService.State.Stopping,
                BaseService.State.Connected, BaseService.State.Connecting).indexOf(ponyStatus)
        }

        fun refreshPonyStatus(index: Int) {
            ponyStatus = arrayListOf<BaseService.State>(BaseService.State.Stopped, BaseService.State.Stopping,
                BaseService.State.Connected, BaseService.State.Connecting)[index]
        }

        fun String.toast(context: Context) {
            Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
        }

        fun isSame(content: ChildTopAdapter.ChildTopContent): Boolean {
            val profile = ProfileManager.getProfile(DataStore.profileId)
            return if (content.atAuto) {
                (profile == null || profile.inAuto || notAutoList.count {
                    !it.atAuto
                            && it.ip == profile.host && it.port == profile.remotePort
                } == 0)
            } else {
                profile != null && !profile.inAuto && profile.host == content.ip && profile.remotePort == content.port
            }
        }

        fun clearDataStore() {
            val profile = ProfileManager.getProfile(DataStore.profileId)
            if (profile != null) {
                ProfileManager.delProfile(profile.id)
            }
        }

        fun refreshDataStore(content: ChildTopAdapter.ChildTopContent) {
            val profile =
                ProfileManager.getProfile(DataStore.profileId) ?: ProfileManager.createProfile()
            profile.host = content.ip
            profile.remotePort = content.port
            profile.method = content.which
            profile.password = content.word
            profile.inAuto = content.atAuto
            profile.country = content.country
            profile.city = content.city
            profile.name = content.country + " " + content.city
            DataStore.profileId = profile.id
            ProfileManager.updateProfile(profile)
        }

        fun profileToContent(profile: Profile?) : ChildTopAdapter.ChildTopContent? {
            if (profile == null) return null
            return ChildTopAdapter.ChildTopContent().apply {
                ip = profile.host
                port = profile.remotePort
                which = profile.method
                word = profile.password
                atAuto = profile.inAuto
                country = profile.country
                city = profile.city
            }
        }

        fun startService() {
            val profile = ProfileManager.getProfile(DataStore.profileId)
            if (profile == null || profile.inAuto || notAutoList.count {
                    !it.atAuto
                            && it.ip == profile.host && it.port == profile.remotePort
                } == 0) {
                val content = autoList.random()
                refreshDataStore(content)
            }
            Core.startService()
        }

        fun stopService() {
            Core.stopService()
        }

        fun Context.showPonyDialog(title: String, negative: String, limited: Boolean, positive: String, ponyYes: () -> Unit) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(title)
            if (negative.isNotEmpty()) {
                builder.setNegativeButton(
                    negative
                ) { dialog, which ->
                    dialog.dismiss()
                }
            }
            builder.setPositiveButton(positive)  { dialog, which ->
                dialog.dismiss()
                ponyYes.invoke()
            }
            val dialog = builder.create()
            if (limited) {
                dialog.setCancelable(false)
                dialog.setCanceledOnTouchOutside(false)
            }
            dialog.show()
        }
    }
}