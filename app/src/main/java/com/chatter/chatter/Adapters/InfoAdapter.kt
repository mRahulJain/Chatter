package com.chatter.chatter.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.chatter.chatter.Activities.ProfileAct
import com.chatter.chatter.Database.AppDatabase
import com.chatter.chatter.Objects_Classes.Profiles
import com.chatter.chatter.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.info_item.view.*

class InfoAdapter(val context: Context, val nameList: ArrayList<Profiles?>, val friendList : ArrayList<String>) : RecyclerView.Adapter<InfoAdapter.NameViewHolder>() {

    val db: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "User.db"
        ).allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }

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
        if(friendList.contains(nameList[position]!!.uid)) {
            holder.itemView.addFriend.text = "Friend"
        }
        if(nameList[position]!!.uid == db.UserDao().getUser()!!.uid) {
            holder.itemView.addFriend.text = "Self"
        }
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

        holder.itemView.addFriend.setOnClickListener {
            if(holder.itemView.addFriend.text.toString() == "Friend" ||
                holder.itemView.addFriend.text.toString() == "Self") {
                return@setOnClickListener
            }
            val reff = FirebaseDatabase.getInstance()
                .getReference("Friends/${db.UserDao().getUser()!!.uid}")
            reff.child("${System.currentTimeMillis()}").setValue(nameList[position]!!.uid)
            val refff = FirebaseDatabase.getInstance()
                .getReference("Friends/${nameList[position]!!.uid}")
            refff.child("${System.currentTimeMillis()}").setValue(db.UserDao().getUser()!!.uid)
            val reffff = FirebaseDatabase.getInstance()
                .getReference("InitialChats/${db.UserDao().getUser()!!.uid}")
            val gName = db.UserDao().getUser().username + "-" + nameList[position]!!.username
            val hashMap = HashMap<String, String>()
            hashMap.put("roomName", "${gName}")
            hashMap.put("roomCode", "")
            hashMap.put("roomAdmin", "${db.UserDao().getUser().username}")
            hashMap.put("roomImg", "")
            reffff.child("${gName}").setValue(hashMap)
            val refffff = FirebaseDatabase.getInstance()
                .getReference("InitialChats/${nameList[position]!!.uid}")
            val hashMap1 = HashMap<String, String>()
            hashMap1.put("roomName", "${gName}")
            hashMap1.put("roomCode", "")
            hashMap1.put("roomAdmin", "${db.UserDao().getUser().username}")
            hashMap1.put("roomImg", "")
            refffff.child("${gName}").setValue(hashMap1)
            val ref1 = FirebaseDatabase.getInstance()
                .getReference("RoomInfo/${gName}")
            val h1 = HashMap<String, String>()
            h1.put("roomName", "${gName}")
            h1.put("roomCode", "")
            h1.put("roomAdmin", "${db.UserDao().getUser().username}")
            h1.put("roomImg", "")
            ref1.setValue(h1)
        }
    }


    class NameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}