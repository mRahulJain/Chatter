package com.chatter.chatter.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chatter.chatter.R
import kotlinx.android.synthetic.main.item_chats.view.*

class LoadingAdapter(val context: Context, val check : Boolean) :
    RecyclerView.Adapter<LoadingAdapter.NameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameViewHolder {
        val li = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = li.inflate(R.layout.item_chats, parent, false)
        return NameViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        if(check == true) {
            return 20
        }
        return 0
    }

    override fun onBindViewHolder(holder: LoadingAdapter.NameViewHolder, position: Int) {
        if(check == true) {
            holder.itemView.parentLayoutShimmer.startShimmerAnimation()
            holder.itemView.groupImgI.setBackgroundResource(R.drawable.background_loader)
            holder.itemView.groupNameI.setBackgroundResource(R.drawable.background_loader)
            holder.itemView.groupMessageI.setBackgroundResource(R.drawable.background_loader)
        } else {
            holder.itemView.parentLayoutShimmer.stopShimmerAnimation()
            holder.itemView.groupImgI.setBackgroundResource(R.drawable.background_circle)
            holder.itemView.groupNameI.setBackgroundResource(0)
            holder.itemView.groupMessageI.setBackgroundResource(0)
        }
    }


    inner class NameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}