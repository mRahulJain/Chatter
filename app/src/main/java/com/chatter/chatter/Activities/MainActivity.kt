package com.chatter.chatter.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.core.text.bold
import androidx.room.Room
import com.chatter.chatter.Database.AppDatabase
import com.chatter.chatter.R
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

        createAccount.setOnClickListener {
            val intent = Intent(this, CreateAccountAct::class.java)
            startActivity(intent)
        }

    }
}
