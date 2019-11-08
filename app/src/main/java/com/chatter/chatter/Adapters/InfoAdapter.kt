package com.chatter.chatter.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.chatter.chatter.Activities.ProfileAct
import com.chatter.chatter.Objects_Classes.Profiles
import com.chatter.chatter.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.info_item.view.*

class InfoAdapter(val context: Context, val nameList: ArrayList<Profiles?>) : RecyclerView.Adapter<InfoAdapter.NameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameViewHolder {
        val li = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = li.inflate(R.layout.info_item, parent, false)
        return NameViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        if(nameList == null) {
            return 0
        }
        return nameList.size
    }

    override fun onBindViewHolder(holder: InfoAdapter.NameViewHolder, position: Int) {
        if(nameList == null) {
            return
        }
        if(nameList[position]!!.imageURL != "") {
            Picasso.with(context).load(nameList[position]!!.imageURL)
                .fit()
                .into(holder.itemView.imgPersonP)
        }
        holder.itemView.tViewUserNameP.text = nameList[position]!!.fullName

        holder.itemView.prof.setOnClickListener {
            val intent = Intent(context, ProfileAct::class.java)
            intent.putExtra("uid", "${nameList[position]!!.uid}")
            ContextCompat.startActivity(context, intent, null)
        }
    }


    class NameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}