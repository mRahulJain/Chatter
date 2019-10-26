package com.chatter.chatter.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.Toast
import androidx.core.text.bold
import androidx.core.view.isVisible
import androidx.room.Room
import com.chatter.chatter.Database.AppDatabase
import com.chatter.chatter.Database.User
import com.chatter.chatter.Objects_Classes.Profiles
import com.chatter.chatter.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val db: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "User.db"
        ).allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }
    var checkUser : Int = 0
    lateinit var userNow : Profiles

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val getUser = db.UserDao().getUser()

        if(getUser != null) {
            val intent = Intent(this@MainActivity, ChatsActivity::class.java)
            startActivity(intent)
            finish()
        }

        var create = SpannableStringBuilder()
            .append("Don't have an account? ")
            .bold {
                append("Create here!")
            }
        var forgot = SpannableStringBuilder()
            .append("Forgot your password? ")
            .bold {
                append("Get help!")
            }
        createAccount.text = create
        forgotPassword.text = forgot

        login.setOnClickListener {
            progressLogin.isVisible = true
            login.isVisible = false
            val userRef = FirebaseDatabase.getInstance()
                .getReference("Profiles")
            userRef.addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists()) {
                        for(snap in p0.children) {
                            val getUser = snap.getValue(Profiles::class.java)
                            if(usernameE.text.toString() == getUser!!.username) {
                                if(passwordE.text.toString() == getUser!!.password) {
                                    checkUser = 1
                                    userNow = getUser
                                    break
                                } else {
                                    Toast.makeText(this@MainActivity,
                                        "Password is incorrect",
                                        Toast.LENGTH_SHORT).show()
                                    progressLogin.isVisible = false
                                    login.isVisible = true
                                    checkUser = 2
                                    break
                                }
                            }
                        }
                        if(checkUser == 0) {
                            Toast.makeText(this@MainActivity,
                                "User doesn't exist",
                                Toast.LENGTH_SHORT).show()
                            progressLogin.isVisible = false
                            login.isVisible = true
                        } else if(checkUser == 1) {
                            val user = User(
                                fullName = userNow!!.fullName,
                                username = userNow!!.username,
                                dob = userNow!!.dob,
                                password = userNow!!.password,
                                gender = userNow!!.gender,
                                uid = userNow!!.uid
                            )
                            db.UserDao().insertRow(user)
                            val intent = Intent(this@MainActivity, ChatsActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else if(checkUser == 2) {
                            checkUser = 0
                        }
                    } else {
                        Log.d("myCHECK", "NOOO")
                    }
                }
            })

        }

        createAccount.setOnClickListener {
            val intent = Intent(this, CreateAccountAct::class.java)
            startActivity(intent)
        }

    }
}
