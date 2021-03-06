package com.chatter.chatter.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.room.Room
import com.chatter.chatter.Database.AppDatabase
import com.chatter.chatter.Objects_Classes.Rooms
import com.chatter.chatter.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_group.*
import kotlin.properties.Delegates

class GroupAct : AppCompatActivity() {

    val roomList : ArrayList<String> = arrayListOf()
    val rooms : ArrayList<Rooms> = arrayListOf()
    var check = 0
    var count = 0
    val db: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "User.db"
        ).allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }
    lateinit var uid : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        val type = intent.getStringExtra("type")
        intentType.text = type
        if(type == "Create a group") {
            action.setText("Create")
        } else {
            action.setText("Join")
        }

        val dbUser = db.UserDao().getUser()
        uid = dbUser!!.uid

        action.setOnClickListener {
            if(groupName.text.toString() == "" ||
                    groupPasskey.text.toString() == "") {
                Toast.makeText(this@GroupAct,
                    "Enter above credentials",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            action.isVisible = false
            progressGroup.isVisible = true
            if(type == "Create a group") {
                val ref = FirebaseDatabase.getInstance()
                    .getReference("InitialChats/${uid}")
                ref.addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                            roomList.clear()
                            for(snap in p0.children) {
                                roomList.add(snap.key.toString())
                            }

                            if(roomList.contains(groupName.text.toString())) {
                                Snackbar.make(action,
                                    "Group name already taken",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                action.isVisible = true
                                progressGroup.isVisible = false
                            } else {
                                val hashMap = HashMap<String, String>()
                                hashMap.put("roomName", "${groupName.text}")
                                hashMap.put("roomCode", "${groupPasskey.text}")
                                hashMap.put("roomAdmin", "${db.UserDao().getUser().username}")
                                hashMap.put("roomImg", "")
                                ref.child("${groupName.text}").setValue(hashMap)
                                val reff = FirebaseDatabase.getInstance()
                                    .getReference("RoomInfo/${groupName.text}")
                                reff.setValue(hashMap)
                                Toast.makeText(this@GroupAct,
                                    "Group Created Successfully",
                                    Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                })
            } else {
                val ref = FirebaseDatabase.getInstance()
                    .getReference("RoomInfo")
                ref.addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if(p0.exists()) {
                            check = 0
                            rooms.clear()
                            for(snap in p0.children) {
                                val getRoom = snap.getValue(Rooms::class.java)
                                if(getRoom!!.roomName == groupName.text.toString()) {
                                    if(getRoom!!.roomCode == groupPasskey.text.toString()) {
                                        if(count == 0) {
                                            val reff = FirebaseDatabase.getInstance()
                                                .getReference("InitialChats/${uid}")
                                            val hashMap = HashMap<String, String>()
                                            hashMap.put("roomName", "${groupName.text}")
                                            hashMap.put("roomCode", "${groupPasskey.text}")
                                            hashMap.put("roomAdmin", "${db.UserDao().getUser().username}")
                                            hashMap.put("roomImg", "")
                                            reff.child(groupName.text.toString()).setValue(hashMap)
                                            Toast.makeText(this@GroupAct,
                                                "Group Joined Successfully",
                                                Toast.LENGTH_SHORT).show()
                                            finish()
                                            count = 1
                                        }
                                    } else {
                                        Snackbar.make(action,
                                            "Passkey's wrong",
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                        action.isVisible = true
                                        progressGroup.isVisible = false
                                    }
                                    check = 1
                                }
                            }

                            if(check == 0) {
                                Snackbar.make(action,
                                    "No such group found",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                action.isVisible = true
                                progressGroup.isVisible = false
                            }
                        }
                    }
                })
            }
        }

    }
}
