package com.bubble.pollypony.mutChild

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bubble.pollypony.R
import com.bubble.pollypony.mut.ChildTopAdapter
import com.bubble.pollypony.start.PonyParams.Companion.isSame

class ChildContentAdapter : RecyclerView.Adapter<ChildContentAdapter.ChildContentViewHolder>() {

    class ChildData {
        var data: MutableList<ChildTopAdapter.ChildTopContent> = mutableListOf()

        companion object {
            fun buildFlag(content: String): Int {
                val code = content.replace("\\s".toRegex(), "").toLowerCase()
                return when (code) {
                    "australia" -> R.mipmap.australia
                    "belgium" -> R.mipmap.belgium
                    "brazil" -> R.mipmap.brazil
                    "canada" -> R.mipmap.canada
                    "france" -> R.mipmap.france
                    "germany" -> R.mipmap.germany
                    "hongkong" -> R.mipmap.hongkong
                    "india" -> R.mipmap.india
                    "ireland" -> R.mipmap.ireland
                    "italy" -> R.mipmap.italy
                    "japan" -> R.mipmap.japan
                    "koreasouth" -> R.mipmap.koreasouth
                    "netherlands" -> R.mipmap.netherlands
                    "newzealand" -> R.mipmap.newzealand
                    "norway" -> R.mipmap.norway
                    "russianfederation" -> R.mipmap.russianfederation
                    "singapore" -> R.mipmap.singapore
                    "sweden" -> R.mipmap.sweden
                    "switzerland" -> R.mipmap.switzerland
                    "unitedarabemirates" -> R.mipmap.unitedarabemirates
                    "unitedkingdom" -> R.mipmap.unitedkingdom
                    "unitedstates" -> R.mipmap.unitedstates
                    else -> R.mipmap.c_auto
                }
            }
        }
    }

    var data: ChildData = ChildData()

    fun initTouchNotAuto(touchNotAuto: ((Boolean, ChildTopAdapter.ChildTopContent) -> Unit)? = null) {
        this.touchNotAuto = touchNotAuto
    }

    var touchNotAuto: ((Boolean, ChildTopAdapter.ChildTopContent) -> Unit)? = null

    fun initChildData(d: MutableList<ChildTopAdapter.ChildTopContent>) {
        data.data.clear()
        data.data.addAll(d)
    }

    class ChildContentViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var childCon: TextView
        var childCec: ImageView
        var childRoot: LinearLayout

        init {
            childCon = v.findViewById(R.id.child_con)
            childCec = v.findViewById(R.id.child_cec)
            childRoot = v.findViewById(R.id.child_root)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildContentViewHolder {
        return ChildContentViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_mutile_child, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return data.data.size
    }

    override fun onBindViewHolder(holder: ChildContentViewHolder, position: Int) {
        val d = data.data[position]
        holder.childCon.text = d.city + "-" + d.random
        holder.childCec.isSelected = isSame(d)
        holder.childCec.setImageResource(if (holder.childCec.isSelected) R.mipmap.c_mutile_child_status1 else R.mipmap.c_mutile_child_status0)
        holder.childRoot.setOnClickListener {
            touchNotAuto?.invoke(holder.childCec.isSelected, d)
        }
    }


}