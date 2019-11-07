package com.chatter.chatter.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.chatter.chatter.Objects_Classes.Profiles
import com.chatter.chatter.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.info_item.view.*

class LoadingInfoAdapter(val context: Context) : RecyclerView.Adapter<LoadingInfoAdapter.NameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameViewHolder {
        val li = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = li.inflate(R.layout.info_item, parent, false)
        return NameViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return 6
    }

    override fun onBindViewHolder(holder: LoadingInfoAdapter.NameViewHolder, position: Int) {
        holder.itemView.shimmerProfiles.startShimmerAnimation()
    }


    class NameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}