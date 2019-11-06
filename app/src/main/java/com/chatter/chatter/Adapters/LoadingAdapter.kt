package com.chatter.chatter.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chatter.chatter.R
import kotlinx.android.synthetic.main.item_chats.view.*

class LoadingAdapter(val context: Context) :
    RecyclerView.Adapter<LoadingAdapter.NameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameViewHolder {
        val li = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = li.inflate(R.layout.item_chats, parent, false)
        return NameViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return 20
    }

    override fun onBindViewHolder(holder: LoadingAdapter.NameViewHolder, position: Int) {
        holder.itemView.parentLayoutShimmer.startShimmerAnimation()
        holder.itemView.groupImgI.setBackgroundResource(R.drawable.background_loader)
        holder.itemView.groupNameI.setBackgroundResource(R.drawable.background_loader)
    }


    inner class NameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}