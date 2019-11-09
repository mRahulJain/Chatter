package com.chatter.chatter.Activities

import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.room.Room
import com.chatter.chatter.Adapters.InfoAdapter
import com.chatter.chatter.Database.AppDatabase
import com.chatter.chatter.Objects_Classes.Profiles
import com.chatter.chatter.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_add_people.*

class AddPeopleAct : AppCompatActivity() {

    val db: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "User.db"
        ).allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }
    val friendList : ArrayList<String> = arrayListOf()
    val users : ArrayList<Profiles?> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_people)

        searchFriendS.setOnClickListener {
            val ref = FirebaseDatabase.getInstance()
                .getReference("Profiles")
            ref.addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists()) {
                        users.clear()
                        for(snap in p0.children) {
                            val getProfile = snap.getValue(Profiles::class.java)
                            if(getProfile!!.username == usernameS.text.toString() ||
                                getProfile!!.fullName.contains(usernameS.text.toString())) {
                                users.add(getProfile!!)
                            }
                        }

                        val reff = FirebaseDatabase.getInstance()
                            .getReference("Friends/${db.UserDao().getUser()!!.uid}")
                        reff.addValueEventListener(object : ValueEventListener{
                            override fun onCancelled(p0: DatabaseError) {

                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                if(p0.exists()) {
                                    for(snap in p0.children) {
                                        friendList.add(snap.value.toString())
                                    }
                                }
                                noUserFound.isVisible = users.size == 0
                                friendRView.layoutManager = GridLayoutManager(this@AddPeopleAct,
                                    2, GridLayoutManager.VERTICAL, false)
                                friendRView.adapter = InfoAdapter(this@AddPeopleAct, users, friendList)
                            }

                        })
                    }
                }

            })
        }
    }
}
