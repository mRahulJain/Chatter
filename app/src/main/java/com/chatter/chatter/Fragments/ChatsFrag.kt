package com.chatter.chatter.Fragments


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.chatter.chatter.Activities.GroupAct
import com.chatter.chatter.Adapters.ChatAdapter
import com.chatter.chatter.Adapters.LoadingAdapter
import com.chatter.chatter.Objects_Classes.Rooms

import com.chatter.chatter.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_chats.view.*

class ChatsFrag : Fragment() {

    val roomList : ArrayList<String> = arrayListOf()
    val groupList : ArrayList<Rooms> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chats, container, false)

        view!!.listChats.layoutManager = LinearLayoutManager(view!!.context, LinearLayoutManager.VERTICAL, false)
        view!!.listChats.adapter = LoadingAdapter(view!!.context)

        val ref = FirebaseDatabase.getInstance()
            .getReference("Groups/${FirebaseAuth.getInstance().currentUser!!.uid}")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()) {
                    roomList.clear()
                    for(snap in p0.children) {
                        roomList.add(snap.value.toString())
                    }

                    val reff = FirebaseDatabase.getInstance()
                        .getReference("Rooms")
                    reff.addValueEventListener(object : ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if(p0.exists()) {
                                groupList.clear()
                                for(snap in p0.children) {
                                    if(roomList.contains(snap.key.toString())) {
                                        groupList.add(snap.getValue(Rooms::class.java)!!)
                                    }
                                }
                                view!!.listChats.layoutManager = LinearLayoutManager(view!!.context, LinearLayoutManager.VERTICAL, false)
                                view!!.listChats.adapter = ChatAdapter(view!!.context, groupList)
                                view!!.listChats.adapter!!.notifyDataSetChanged()
                            }
                        }

                    })
                } else {
                    view!!.noChats.isVisible = true
                }
            }

        })

        val mShowButton = AnimationUtils.loadAnimation(view!!.context, R.anim.anim)
        val mRemoveButton = AnimationUtils.loadAnimation(view!!.context, R.anim.anim2)
        val mfadeIn = AnimationUtils.loadAnimation(view!!.context, R.anim.anim3)
        val mFadeOut = AnimationUtils.loadAnimation(view!!.context, R.anim.anim4)

        view!!.plus.setOnClickListener {
            if(view!!.createRoom.isVisible == true) {
                view!!.createRoom.isVisible = false
                view!!.joinRoom.isVisible = false
                view!!.addFriend.isVisible = false
                view!!.createRoom.startAnimation(mFadeOut)
                view!!.joinRoom.startAnimation(mFadeOut)
                view!!.addFriend.startAnimation(mFadeOut)
                view!!.plus.startAnimation(mRemoveButton)
            } else {
                view!!.createRoom.isVisible = true
                view!!.joinRoom.isVisible = true
                view!!.addFriend.isVisible = true
                view!!.createRoom.startAnimation(mfadeIn)
                view!!.joinRoom.startAnimation(mfadeIn)
                view!!.addFriend.startAnimation(mfadeIn)
                view!!.plus.startAnimation(mShowButton)
            }
        }

        view!!.createRoom.setOnClickListener {
            val intent = Intent(view!!.context, GroupAct::class.java)
            intent.putExtra("type", "Create a group")
            view!!.createRoom.isVisible = false
            view!!.joinRoom.isVisible = false
            view!!.addFriend.isVisible = false
            view!!.createRoom.startAnimation(mFadeOut)
            view!!.joinRoom.startAnimation(mFadeOut)
            view!!.addFriend.startAnimation(mFadeOut)
            view!!.plus.startAnimation(mRemoveButton)
            startActivity(intent)
        }

        view!!.joinRoom.setOnClickListener {
            val intent = Intent(view!!.context, GroupAct::class.java)
            intent.putExtra("type", "Join a group")
            view!!.createRoom.isVisible = false
            view!!.joinRoom.isVisible = false
            view!!.addFriend.isVisible = false
            view!!.createRoom.startAnimation(mFadeOut)
            view!!.joinRoom.startAnimation(mFadeOut)
            view!!.addFriend.startAnimation(mFadeOut)
            view!!.plus.startAnimation(mRemoveButton)
            startActivity(intent)
        }

        return view
    }


}
