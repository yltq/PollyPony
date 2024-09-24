package com.bubble.pollypony.start

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.lifecycleScope
import com.bubble.pollypony.R
import com.bubble.pollypony.mut.ChildTopAdapter
import com.bubble.pollypony.mutChild.ChildContentAdapter
import com.bubble.pollypony.mutChild.ChildContentAdapter.ChildData.Companion.buildFlag
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.DataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OnFindActivity : AppCompatActivity() {
    private lateinit var c_dark_back: ImageView
    private lateinit var on_find_alpha_center: LinearLayoutCompat
    private lateinit var on_find_count: TextView
    private lateinit var on_find_result: TextView
    private lateinit var on_find_on: LinearLayoutCompat
    private lateinit var on_find_country: ImageView
    private lateinit var ping: TextView
    private lateinit var on_find_country_detail: TextView
    private lateinit var on_find_city: TextView

    private lateinit var on_find_off: LinearLayoutCompat
    private lateinit var on_find_reon: ImageView
    private lateinit var recommend_1_logo: ImageView
    private lateinit var recommend_1_country: TextView

    private lateinit var recommend_2_logo: ImageView
    private lateinit var recommend_2_country: TextView

    private lateinit var recommend_3_logo: ImageView
    private lateinit var recommend_3_country: TextView

    private lateinit var recommend_1: LinearLayoutCompat
    private lateinit var recommend_2: LinearLayoutCompat
    private lateinit var recommend_3: LinearLayoutCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_find)
        c_dark_back = findViewById(R.id.c_dark_back)
        on_find_alpha_center = findViewById(R.id.on_find_alpha_center)
        on_find_count = findViewById(R.id.on_find_count)
        on_find_result = findViewById(R.id.on_find_result)
        on_find_on = findViewById(R.id.on_find_on)
        on_find_country = findViewById(R.id.on_find_country)
        ping = findViewById(R.id.ping)
        on_find_country_detail = findViewById(R.id.on_find_country_detail)
        on_find_city = findViewById(R.id.on_find_city)
        on_find_off = findViewById(R.id.on_find_off)
        on_find_reon = findViewById(R.id.on_find_reon)
        recommend_1_logo = findViewById(R.id.recommend_1_logo)
        recommend_1_country = findViewById(R.id.recommend_1_country)
        recommend_2_logo = findViewById(R.id.recommend_2_logo)
        recommend_2_country = findViewById(R.id.recommend_2_country)
        recommend_3_logo = findViewById(R.id.recommend_3_logo)
        recommend_3_country = findViewById(R.id.recommend_3_country)
        recommend_1 = findViewById(R.id.recommend_1)
        recommend_2 = findViewById(R.id.recommend_2)
        recommend_3 = findViewById(R.id.recommend_3)

        c_dark_back.setOnClickListener {
            finish()
        }
        val profile = ProfileManager.getProfile(DataStore.profileId)
        if (profile == null || profile.inAuto || PonyParams.notAutoList.count {
                !it.atAuto
                        && it.ip == profile.host && it.port == profile.remotePort
            } == 0) {
            on_find_country.setImageResource(R.mipmap.c_auto)
            on_find_country_detail.text = profile?.country?:"Optimal Node"
            on_find_city.text = profile?.city?:"-"
        } else {
            on_find_country.setImageResource(ChildContentAdapter.ChildData.buildFlag(profile.country))
            on_find_country_detail.text = profile.country
            on_find_city.text = profile.city
        }
        if (PonyParams.ponyConnected) {
            on_find_alpha_center.alpha = 1f
            on_find_result.text = getString(R.string.pony_con_success)
            on_find_on.visibility = View.VISIBLE
            on_find_off.visibility = View.GONE
            PonySpeedParams.ponyPing {
                lifecycleScope.launch(Dispatchers.Main) {
                    ping.text = it
                }
            }
            ViewParams.ponyCountLivedata.observe(this) {
                val hours = it / 3600
                val minutes = it % 3600 / 60
                val seconds = it % 3600 % 60
                on_find_count.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }
        } else {
            intent.extras?.also {
                val count = it.getString("count")
                on_find_count.text = count
            }
            on_find_alpha_center.alpha = 0.4f
            on_find_result.text = getString(R.string.pony_dis_success)
            on_find_on.visibility = View.GONE
            on_find_off.visibility = View.VISIBLE
            val bestList = mutableListOf<ChildTopAdapter.ChildTopContent>()
            if (PonyParams.notAutoList.isNotEmpty() && PonyParams.notAutoList.size >= 3) {
                val autoSize = PonyParams.autoList.size
                if (autoSize == 0) {
                    bestList.addAll(PonyParams.notAutoList.take(3))
                } else if (autoSize == 1) {
                    bestList.addAll(PonyParams.autoList.take(1))
                    bestList.addAll(PonyParams.notAutoList.take(2))
                } else if (autoSize == 2) {
                    bestList.addAll(PonyParams.autoList.take(2))
                    bestList.addAll(PonyParams.notAutoList.take(1))
                } else if (autoSize == 3) {
                    bestList.addAll(PonyParams.autoList.take(3))
                }
                recommend_1_logo.setImageResource(buildFlag(bestList[0].country))
                recommend_1_country.text = bestList[0].country + " " + bestList[0].city

                recommend_2_logo.setImageResource(buildFlag(bestList[1].country))
                recommend_2_country.text = bestList[1].country + " " + bestList[1].city

                recommend_3_logo.setImageResource(buildFlag(bestList[2].country))
                recommend_3_country.text = bestList[2].country + " " + bestList[2].city

                recommend_1.setOnClickListener {
                    PonyParams.refreshDataStore(bestList[0])
                    // to connect
                    val intent = Intent()
                    intent.putExtra("connect", true)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
                recommend_2.setOnClickListener {
                    PonyParams.refreshDataStore(bestList[1])
                    // to connect
                    val intent = Intent()
                    intent.putExtra("connect", true)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
                recommend_3.setOnClickListener {
                    PonyParams.refreshDataStore(bestList[2])
                    // to connect
                    val intent = Intent()
                    intent.putExtra("connect", true)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }

            }

            on_find_reon.setOnClickListener {
                // to connect
                val intent = Intent()
                intent.putExtra("connect", true)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

    }
}