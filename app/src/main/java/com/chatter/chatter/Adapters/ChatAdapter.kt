package com.chatter.chatter.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chatter.chatter.Activities.ConvoActivity
import com.chatter.chatter.Objects_Classes.Rooms
import com.chatter.chatter.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_chats.view.*

class ChatAdapter(val context: Context, val nameList : ArrayList<Rooms>) :
    RecyclerView.Adapter<ChatAdapter.NameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameViewHolder {
        val li = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = li.inflate(R.layout.item_chats, parent, false)
        return NameViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return nameList.size
    }

    override fun onBindViewHolder(holder: ChatAdapter.NameViewHolder, position: Int) {
        holder.itemView.groupNameI.text = nameList[position]!!.roomName
        if(nameList[position]!!.roomImg == "") {
            holder.itemView.groupImgI.setImageResource(R.drawable.ic_group)
        } else {
            Picasso.with(context)
                .load(nameList[position]!!.roomImg)
                .into(holder.itemView.groupImgI)
        }

        holder.itemView.openConversation.setOnClickListener {
            val intent = Intent(context, ConvoActivity::class.java)
            intent.putExtra("roomName", "${nameList[position]!!.roomName}")
            context.startActivity(intent)
        }
    }


    inner class NameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}