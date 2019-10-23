package com.chatter.chatter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.core.text.bold
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
    }
}
