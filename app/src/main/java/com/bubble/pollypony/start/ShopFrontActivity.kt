package com.bubble.pollypony.start

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceDataStore
import com.airbnb.lottie.LottieAnimationView
import com.bubble.pollypony.R
import com.bubble.pollypony.mutChild.ChildContentAdapter.ChildData.Companion.buildFlag
import com.bubble.pollypony.start.PonyParams.Companion.ponyStatusIndex
import com.bubble.pollypony.start.PonyParams.Companion.showPonyDialog
import com.bubble.pollypony.start.PonyParams.Companion.toast
import com.bubble.pollypony.web.PollyPony
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.DataStore
import com.github.shadowsocks.preference.OnPreferenceDataStoreChangeListener
import com.github.shadowsocks.utils.Key
import com.github.shadowsocks.utils.StartService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ShopFrontActivity : AppCompatActivity(), OnPreferenceDataStoreChangeListener,
    ShadowsocksConnection.Callback {
    protected lateinit var connection: ShadowsocksConnection
    private lateinit var set: ImageView
    private lateinit var shop_country_logo: ImageView
    private lateinit var shop_country: TextView
    private lateinit var shop_count: TextView
    private lateinit var shop_connect_t: TextView
    private lateinit var c_connect: ImageView
    private lateinit var shop_country_layout: LinearLayoutCompat

    private lateinit var b_off_layout: LinearLayoutCompat
    private lateinit var b_loading_layout: LinearLayoutCompat
    private lateinit var b_on_layout: LinearLayoutCompat
    private lateinit var on_test: LinearLayoutCompat
    private lateinit var shop_test: LinearLayoutCompat
    private lateinit var on_find_running: FrameLayout
    private lateinit var pony_cold_mask: FrameLayout
    private lateinit var pony_touch: LinearLayoutCompat
    private lateinit var pony_v: LottieAnimationView


    private lateinit var c_down: TextView
    private lateinit var c_up: TextView
    private var goJob: Job? = null

    private var connectRandom: Boolean = true

    protected var startService = registerForActivityResult(StartService()) {
        if (it) return@registerForActivityResult
        goConnect()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataStore.publicStore.registerChangeListener(this)
        connection = ShadowsocksConnection(true).run {
            bandwidthTimeout = 500L
            connect(this@ShopFrontActivity, this@ShopFrontActivity)
            this
        }
        setContentView(R.layout.activity_shop_front)
        set = findViewById(R.id.set)
        shop_country_logo = findViewById(R.id.shop_country_logo)
        shop_country = findViewById(R.id.shop_country)
        shop_count = findViewById(R.id.shop_count)
        shop_connect_t = findViewById(R.id.shop_connect_t)
        shop_country_layout = findViewById(R.id.shop_country_layout)
        c_connect = findViewById(R.id.c_connect)
        b_off_layout = findViewById(R.id.b_off_layout)
        b_loading_layout = findViewById(R.id.b_loading_layout)
        b_on_layout = findViewById(R.id.b_on_layout)
        on_test = findViewById(R.id.on_test)
        c_down = findViewById(R.id.c_down)
        c_up = findViewById(R.id.c_up)
        shop_test = findViewById(R.id.shop_test)
        on_find_running = findViewById(R.id.on_find_running)
        pony_cold_mask = findViewById(R.id.pony_cold_mask)
        pony_touch = findViewById(R.id.pony_touch)
        pony_v = findViewById(R.id.pony_v)

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (pony_cold_mask.visibility == View.VISIBLE) {
                    pony_cold_mask.visibility = View.GONE
                } else {
                    centerTouch {
                        finish()
                    }
                }
            }

        })
        if (PonyParams.ponyConnected) {
            b_loading_layout.visibility = View.GONE
            b_off_layout.visibility = View.GONE
            b_on_layout.visibility = View.VISIBLE
            on_test.visibility = View.VISIBLE
            shop_count.visibility = View.VISIBLE
            shop_connect_t.visibility = View.VISIBLE
            pony_v.visibility = View.GONE
            c_connect.visibility = View.VISIBLE
            c_connect.setImageResource(R.mipmap.c_connect)
        } else {
            b_loading_layout.visibility = View.GONE
            b_off_layout.visibility = View.VISIBLE
            b_on_layout.visibility = View.GONE
            on_test.visibility = View.GONE
            shop_count.visibility = View.INVISIBLE
            shop_connect_t.visibility = View.INVISIBLE
            pony_v.visibility = View.GONE
            c_connect.visibility = View.VISIBLE
            c_connect.setImageResource(R.mipmap.c_disconnect)
        }
        ViewParams.ponyCountLivedata.observe(this) {
            val hours = it / 3600
            val minutes = it % 3600 / 60
            val seconds = it % 3600 % 60
            shop_count.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

        startConnect(false)
        if (PollyPony.showPonyCold && !PonyParams.ponyConnected) {
            PollyPony.showPonyCold = false
            pony_cold_mask.visibility = View.VISIBLE
            pony_touch.setOnClickListener {
                pony_cold_mask.visibility = View.GONE
                centerTouch {
                    startConnect(true)
                }
            }
        } else {
            pony_cold_mask.visibility = View.GONE
        }

        set.setOnClickListener {
            centerTouch {
                startActivity(Intent(this@ShopFrontActivity, AccountActivity::class.java))
            }
        }
        shop_country_layout.setOnClickListener {
            centerTouch {
                onFindResult.launch(Intent(this, MutileActivity::class.java))
            }
        }
        b_off_layout.setOnClickListener {
            centerTouch {
                startConnect(true)
            }
        }
        b_on_layout.setOnClickListener {
            centerTouch {
                startStop()
            }
        }
        shop_test.setOnClickListener {
            var pingV = ""
            var pingDownloadV = ""
            var pingUploadV = ""
            if (on_find_running.visibility == View.VISIBLE) return@setOnClickListener
            on_find_running.visibility = View.VISIBLE
            var waitCount = 0
            lifecycleScope.launch(Dispatchers.IO) {
                repeat(15) {
                    delay(1000)
                    waitCount += 1
                    if (pingV.isNotEmpty() && pingDownloadV.isNotEmpty() && pingUploadV.isNotEmpty()) {
                        cancel()
                        lifecycleScope.launch(Dispatchers.Main) {
                            on_find_running.visibility = View.GONE
                            val intent = Intent(this@ShopFrontActivity, InspectActivity::class.java)
                            intent.putExtra("ping", pingV)
                            intent.putExtra("download", pingDownloadV)
                            intent.putExtra("upload", pingUploadV)
                            startActivity(intent)
                        }
                    }
                }.also {
                    lifecycleScope.launch(Dispatchers.Main) {
                        on_find_running.visibility = View.GONE
                        "Response timeout".toast(this@ShopFrontActivity)
                    }
                }
            }
            PonySpeedParams.ponyPing {
                lifecycleScope.launch(Dispatchers.Main) {
                    pingV = it
                }
            }
            PonySpeedParams.ponyDownload(download = {
                lifecycleScope.launch(Dispatchers.Main) {
                    pingDownloadV = it
                    c_down.text = it
                }

            }, upload = {
                lifecycleScope.launch(Dispatchers.Main) {
                    pingUploadV = it
                    c_up.text = it
                }

            })
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        connection.disconnect(this)
        DataStore.publicStore.unregisterChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        val profile = ProfileManager.getProfile(DataStore.profileId)
        if (profile == null || profile.inAuto || PonyParams.notAutoList.count {
                !it.atAuto
                        && it.ip == profile.host && it.port == profile.remotePort
            } == 0) {
            shop_country_logo.setImageResource(R.mipmap.c_auto)
            shop_country.text = "Optimal Node"
        } else {
            shop_country_logo.setImageResource(buildFlag(profile.country))
            shop_country.text = profile.name
        }
    }

    private fun centerTouch(touch: () -> Unit) {
        if (pony_cold_mask.visibility == View.VISIBLE || on_find_running.visibility == View.VISIBLE) {
            return
        }
        val index = ponyStatusIndex()
        if (index == 1) {
            "Disconnecting... Please wait".toast(this)
        } else if (index == 3) {
            "Connecting... Please wait".toast(this)
        } else {
            touch.invoke()
        }
    }

    override fun onStop() {
        super.onStop()
        val index = ponyStatusIndex()
        goJob?.cancel()
        if (index == 1 || index == 2) {
            b_loading_layout.visibility = View.GONE
            b_off_layout.visibility = View.GONE
            b_on_layout.visibility = View.VISIBLE
            on_test.visibility = View.VISIBLE
            shop_count.visibility = View.VISIBLE
            shop_connect_t.visibility = View.VISIBLE
            pony_v.visibility = View.GONE
            c_connect.visibility = View.VISIBLE
            c_connect.setImageResource(R.mipmap.c_connect)
            PonyParams.refreshPonyStatus(2)
        } else if (index == 3 || index == 0) {
            b_loading_layout.visibility = View.GONE
            b_off_layout.visibility = View.VISIBLE
            b_on_layout.visibility = View.GONE
            on_test.visibility = View.GONE
            shop_count.visibility = View.INVISIBLE
            shop_connect_t.visibility = View.INVISIBLE
            pony_v.visibility = View.GONE
            c_connect.visibility = View.VISIBLE
            c_connect.setImageResource(R.mipmap.c_disconnect)
            PonyParams.refreshPonyStatus(0)
        }
        if (PonyParams.lastContent != null) {
            PonyParams.refreshDataStore(PonyParams.lastContent!!)
            PonyParams.lastContent = null
        }
    }

    private fun goConnect() {
        goJob?.cancel()
        goJob = lifecycleScope.launch {
            PonyParams.refreshPonyStatus(3)
            b_loading_layout.visibility = View.VISIBLE
            b_off_layout.visibility = View.GONE
            b_on_layout.visibility = View.GONE
            on_test.visibility = View.GONE
            shop_count.visibility = View.INVISIBLE
            shop_connect_t.visibility = View.INVISIBLE
            pony_v.visibility = View.VISIBLE
            c_connect.visibility = View.GONE
            repeat(10) {
                delay(200)
            }.also {
                val random = connectRandom
                connectRandom = true
                PonyParams.startService(random)
            }
        }
    }

    private fun startConnect(toConnect: Boolean) {
        PonyParams.webEnable(this) {
            if (!it) {
                //no internet show dialog
                showPonyDialog(getString(R.string.pony_no_network), "", false, getString(R.string.pony_ok)) {

                }
            } else {
                PonyParams.webShort()
                if (PonyParams.inLimited) {
                    //country in limited show dialog and finish
                    showPonyDialog(getString(R.string.pony_restricted), "",true,  getString(R.string.pony_confirm)) {
                        finish()
                    }
                } else if (toConnect) {
                    if (PonyParams.notAutoList.isEmpty()) {
                        //toast no server data and return
                        "no data".toast(this)
                    } else {
                        startService.launch(null)
                    }
                }
            }
        }
    }

    private fun startStop() {
        goJob?.cancel()
        goJob = lifecycleScope.launch {
            PonyParams.refreshPonyStatus(1)
            b_loading_layout.visibility = View.VISIBLE
            b_off_layout.visibility = View.GONE
            b_on_layout.visibility = View.GONE
            on_test.visibility = View.GONE
            shop_count.visibility = View.INVISIBLE
            shop_connect_t.visibility = View.INVISIBLE
            pony_v.visibility = View.VISIBLE
            c_connect.visibility = View.GONE
            repeat(10) {
                delay(200)
            }.also {
                PonyParams.stopService()
            }
        }
    }

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        PonyParams.ponyStatus = state
        val index = PonyParams.ponyStatusIndex()
        if (index == 0) {
            //disconnected
            PonyParams.ponyConnected = false
            b_loading_layout.visibility = View.GONE
            b_off_layout.visibility = View.VISIBLE
            b_on_layout.visibility = View.GONE
            on_test.visibility = View.GONE
            shop_count.visibility = View.INVISIBLE
            shop_connect_t.visibility = View.INVISIBLE
            pony_v.visibility = View.GONE
            c_connect.visibility = View.VISIBLE
            c_connect.setImageResource(R.mipmap.c_disconnect)
            if (PonyParams.lastContent != null) {
                PonyParams.lastContent = null
            }
            ViewParams.ponyCountBegin(false)
            onFindResult.launch(Intent(this, OnFindActivity::class.java).run {
                val hours = ViewParams.ponyCount / 3600
                val minutes = ViewParams.ponyCount % 3600 / 60
                val seconds = ViewParams.ponyCount % 3600 % 60
                putExtra("count", String.format("%02d:%02d:%02d", hours, minutes, seconds))
                this
            })
        } else if (index == 2) {
            //connected
            PonyParams.ponyConnected = true
            b_loading_layout.visibility = View.GONE
            b_off_layout.visibility = View.GONE
            b_on_layout.visibility = View.VISIBLE
            on_test.visibility = View.VISIBLE
            shop_count.visibility = View.VISIBLE
            shop_connect_t.visibility = View.VISIBLE
            pony_v.visibility = View.GONE
            c_connect.visibility = View.VISIBLE
            c_connect.setImageResource(R.mipmap.c_connect)
            ViewParams.ponyCountBegin(true)
            onFindResult.launch(Intent(this, OnFindActivity::class.java))
        }
    }

    override fun onServiceConnected(service: IShadowsocksService) {

    }

    override fun onPreferenceDataStoreChanged(store: PreferenceDataStore, key: String) {
        if (key == Key.serviceMode) {
            connection.disconnect(this)
            connection.connect(this, this)
        }
    }

    private var onFindResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data = it.data?:return@registerForActivityResult
                val connect = data.getBooleanExtra("connect", false)
                val disconnect = data.getBooleanExtra("disconnect", false)
                val connect_not_random = data.getBooleanExtra("connect_not_random", false)
                if (connect) {
                    startConnect(true)
                } else if (disconnect) {
                    startStop()
                } else if (connect_not_random) {
                    connectRandom = false
                    startConnect(true)
                }
            }
        }
}