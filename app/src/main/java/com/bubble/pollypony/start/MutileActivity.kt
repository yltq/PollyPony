package com.bubble.pollypony.start

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bubble.pollypony.R
import com.bubble.pollypony.mut.ChildTopAdapter
import com.bubble.pollypony.start.PonyParams.Companion.showPonyDialog
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.DataStore
import kotlin.random.Random

class MutileActivity : AppCompatActivity() {
    private lateinit var c_light_back: ImageView
    private lateinit var refresh: ImageView
    private lateinit var mutile_view: RecyclerView
    private var adapter: ChildTopAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mutile)
        c_light_back = findViewById(R.id.c_light_back)
        refresh = findViewById(R.id.refresh)
        mutile_view = findViewById(R.id.mutile_view)

        initMutile()
        c_light_back.setOnClickListener {
            finish()
        }
    }

    private fun initMutile() {
        if (PonyParams.notAutoList.isEmpty()) {
            return
        }
        adapter = ChildTopAdapter()
        val map : MutableMap<String, MutableList<ChildTopAdapter.ChildTopContent>> = mutableMapOf()
        map["Optimal Node"] = mutableListOf<ChildTopAdapter.ChildTopContent>()
        PonyParams.notAutoList.forEach {
            if (map.contains(it.country)) {
                val contents = map[it.country]?: mutableListOf()
                it.random = Random.nextInt(10000, 100000)
                contents.add(it)
                map[it.country] = contents
            } else {
                val contents = mutableListOf<ChildTopAdapter.ChildTopContent>()
                it.random = Random.nextInt(10000, 100000)
                contents.add(it)
                map[it.country] = contents
            }
        }
        adapter?.initChildData(map)
        adapter?.initTouchAuto {
             if (it) {
                 if (PonyParams.ponyConnected) {
                     return@initTouchAuto
                 } else {
                     PonyParams.clearDataStore()
                     //to connect
                     val intent = Intent()
                     intent.putExtra("connect", true)
                     setResult(Activity.RESULT_OK, intent)
                     finish()
                 }
             } else {
                 if (PonyParams.ponyConnected) {
                     //show dialog
                     showPonyDialog(getString(R.string.pony_switch), getString(R.string.pony_no),  false, getString(
                         R.string.pony_yes)) {
                         PonyParams.lastContent = PonyParams.profileToContent(ProfileManager.getProfile(DataStore.profileId))
                         PonyParams.clearDataStore()
                         //to disconnect
                         val intent = Intent()
                         intent.putExtra("disconnect", true)
                         setResult(Activity.RESULT_OK, intent)
                         finish()
                     }
                 } else {
                     PonyParams.clearDataStore()
                     // to connect
                     val intent = Intent()
                     intent.putExtra("connect", true)
                     setResult(Activity.RESULT_OK, intent)
                     finish()
                 }
             }
        }
        adapter?.initTouchNotAuto { select, content ->
            if (select) {
                if (PonyParams.ponyConnected) {
                    return@initTouchNotAuto
                } else {
                    // to connect
                    val intent = Intent()
                    intent.putExtra("connect", true)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            } else {
                if (PonyParams.ponyConnected) {
                    //show dialog
                    showPonyDialog(getString(R.string.pony_switch), getString(R.string.pony_no),false,  getString(
                        R.string.pony_yes)) {
                        PonyParams.lastContent = PonyParams.profileToContent(ProfileManager.getProfile(DataStore.profileId))
                        PonyParams.refreshDataStore(content)
                        //to disconnect
                        val intent = Intent()
                        intent.putExtra("disconnect", true)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                } else {
                    PonyParams.refreshDataStore(content)
                    // to connect
                    val intent = Intent()
                    intent.putExtra("connect", true)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }
        mutile_view.adapter = adapter!!
    }
}