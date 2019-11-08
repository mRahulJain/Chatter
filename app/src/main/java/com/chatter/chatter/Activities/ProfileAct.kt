package com.chatter.chatter.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.chatter.chatter.Objects_Classes.Profiles
import com.chatter.chatter.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val uid = intent.getStringExtra("uid")

        shimmerP.startShimmerAnimation()
        val ref = FirebaseDatabase.getInstance()
            .getReference("Profiles/${uid}")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()) {
                    val getProfile = p0.getValue(Profiles::class.java)
                    name2P.setText(getProfile!!.fullName)
                    bioP.setText(getProfile!!.bio)
                    usernameP.setText(getProfile!!.username)
                    gender1P.setText(getProfile!!.gender)
                    dob1P.setText(getProfile!!.dob)
                    shimmerP.stopShimmerAnimation()

                    if(getProfile!!.imageURL != "") {
                        Picasso.with(this@ProfileAct)
                            .load(getProfile!!.imageURL)
                            .into(profilePictureP)

                        profilePictureP.setOnClickListener {
                            val intent = Intent(this@ProfileAct, ImageActivity::class.java)
                            intent.putExtra("purpose", "Profile Picture")
                            intent.putExtra("url", "${getProfile!!.imageURL}")
                            startActivity(intent)
                        }
                    }
                } else {
                    shimmerP.stopShimmerAnimation()
                }
            }

        })
    }

}
