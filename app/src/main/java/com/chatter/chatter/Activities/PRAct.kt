package com.chatter.chatter.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import com.chatter.chatter.Objects_Classes.Profiles
import com.chatter.chatter.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_pr.*

class PRAct : AppCompatActivity() {

    var count = 0
    lateinit var actualUser : Profiles

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pr)

        val username = intent.getStringExtra("username")
        val ref = FirebaseDatabase.getInstance()
            .getReference("Profiles")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()) {
                    for(snap in p0.children) {
                        val user = snap.getValue(Profiles::class.java)
                        if(user!!.username == username) {
                            question.text = user!!.prq
                            count = 1
                            actualUser = user!!
                        }
                    }

                    if(count == 0) {
                        question.setText("No such username exists!!")
                        answer.isVisible = false
                        getPassword.isVisible = false
                    } else {
                        getPassword.setOnClickListener {
                            if(actualUser!!.prqA == answer.text.toString()) {
                                Toast.makeText(this@PRAct,
                                    "${actualUser!!.username}\n${actualUser!!.password}",
                                    Toast.LENGTH_LONG).show()
                                finish()
                            } else {
                                Toast.makeText(this@PRAct,
                                    "Wrong Password",
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }

        })
    }
}
