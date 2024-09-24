package com.bubble.pollypony.mut

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.bubble.pollypony.R
import com.bubble.pollypony.mutChild.ChildContentAdapter
import com.bubble.pollypony.mutChild.ChildContentAdapter.ChildData.Companion.buildFlag
import com.bubble.pollypony.start.PonyParams.Companion.isSame

class ChildTopAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class ChildTopContent {
        var ip: String = ""
        var which: String = ""
        var port: Int = 0
        var word: String = ""
        var country: String = ""
        var city: String = ""
        var atAuto: Boolean = true
        var random: Int = 0
    }

    class ChildData {
        var tops: MutableList<String> = mutableListOf()
        var children: MutableList<MutableList<ChildTopContent>> = mutableListOf()
    }

    private var data: ChildData = ChildData()

    fun initChildData(d: MutableMap<String, MutableList<ChildTopContent>>) {
        if (d.size == 0) return
        data.tops.clear()
        data.children.clear()
        d.forEach { t, u ->
            data.tops.add(t)
            data.children.add(u)
        }
    }

    fun initTouchAuto(touchAuto: (Boolean) -> Unit) {
        this.touchAuto = touchAuto
    }

    fun initTouchNotAuto(touchNotAuto: (Boolean, ChildTopAdapter.ChildTopContent) -> Unit) {
        this.touchNotAuto = touchNotAuto
    }

    var touchAuto: ((Boolean) -> Unit)? = null
    var touchNotAuto: ((Boolean, ChildTopAdapter.ChildTopContent) -> Unit)? = null

    class ChildTopViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        lateinit var root: LinearLayoutCompat
        lateinit var cCountry: ImageView
        lateinit var cCountryType: TextView
        lateinit var cBt: ImageView
        lateinit var child: LinearLayoutCompat
        lateinit var topChild: RecyclerView

        init {
            root = v.findViewById(R.id.top_root)
            cCountry = v.findViewById(R.id.c_country)
            cCountryType = v.findViewById(R.id.c_country_type)
            cBt = v.findViewById(R.id.c_bt)
            child = v.findViewById(R.id.child)
            topChild = v.findViewById(R.id.top_child)
        }
    }

    class ChildTopAutoViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        lateinit var root: LinearLayoutCompat
        lateinit var cBt: ImageView

        init {
            root = v.findViewById(R.id.top_root)
            cBt = v.findViewById(R.id.c_bt)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            return ChildTopAutoViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_mutile_top_auto, parent, false)
            )
        }
        return ChildTopViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_mutile_top, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return data.tops.size
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return 0
        }
        return 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val top = data.tops[position]
        when (holder) {
            is ChildTopAutoViewHolder -> {
                val p = ChildTopContent().apply {
                    atAuto = true
                    country = "Optimal Node"
                }
                holder.cBt.isSelected = isSame(p)
                holder.cBt.setImageResource(if (holder.cBt.isSelected) R.mipmap.c_mutile_child_status1 else R.mipmap.c_mutile_child_status0)
                holder.root.setOnClickListener {
                    touchAuto?.invoke(holder.cBt.isSelected)
                }
            }

            is ChildTopViewHolder -> {
                holder.cCountryType.text = top
                holder.cCountry.setImageResource(buildFlag(top))
                val children = data.children[position]
                val adapter = ChildContentAdapter()
                adapter.initTouchNotAuto(touchNotAuto)
                adapter.initChildData(children)
                holder.topChild.adapter = adapter
                holder.root.setOnClickListener {
                    holder.cBt.isSelected = !holder.cBt.isSelected
                    holder.cBt.setImageResource(if (holder.cBt.isSelected) R.mipmap.c_mutile_top else R.mipmap.c_mutile_bottom)
                    holder.child.visibility = if (holder.cBt.isSelected) View.VISIBLE else View.GONE
                }
            }
        }
    }
}