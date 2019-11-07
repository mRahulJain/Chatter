package com.chatter.chatter.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.chatter.chatter.Activities.ConvoActivity
import com.chatter.chatter.Activities.GroupInfoAct
import com.chatter.chatter.Objects_Classes.Message
import com.chatter.chatter.Objects_Classes.Rooms
import com.chatter.chatter.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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


        val ref = FirebaseDatabase.getInstance()
            .getReference("Chats/${nameList[position]!!.roomName}")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()) {
                    var message = ""
                    for(snap in p0.children) {
                        val getMessage = snap.getValue(Message::class.java)
                        message = getMessage!!.text
                    }
                    holder.itemView.groupMessageI.text = message
                }
            }

        })
        val reff = FirebaseDatabase.getInstance()
            .getReference("RoomInfo/${nameList[position]!!.roomName}")
        reff.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()) {
                    val getInfo = p0.getValue(Rooms::class.java)
                    if(getInfo!!.roomImg == "") {
                        holder.itemView.groupImgI.setImageResource(R.drawable.ic_group)
                    } else {
                        Picasso.with(context)
                            .load(getInfo!!.roomImg)
                            .into(holder.itemView.groupImgI)
                    }
                }
            }

        })

        holder.itemView.openConversation.setOnClickListener {
            val intent = Intent(context, ConvoActivity::class.java)
            intent.putExtra("roomName", "${nameList[position]!!.roomName}")
            context.startActivity(intent)
        }

        holder.itemView.groupImgI.setOnClickListener {
            val intent = Intent(context, GroupInfoAct::class.java)
            intent.putExtra("roomName", "${nameList[position]!!.roomName}")
            context.startActivity(intent)
        }
    }


    inner class NameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}