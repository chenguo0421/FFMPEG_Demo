package com.siwencat.ffmpeg_android_test.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.siwencat.ffmpeg_android_test.R

/**
 * @Description TODO
 * @Creator ChenGuo
 * @Email wushengyuan1hao@163.com
 * @Date 11-04-2022 周五 14:36
 */
class ImageItemAdapter(var context: Context, var list: MutableList<String>):RecyclerView.Adapter<ImageItemAdapter.MyHolder>() {




    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var img:AppCompatImageView = itemView.findViewById(R.id.iv_img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        Glide.with(context).load(list[position]).into(holder.img)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateClipImageList(tempList: MutableList<String>) {
        if (tempList.size > 0) {
            list.clear()
            list.addAll(tempList)
            notifyDataSetChanged()
        }
    }
}