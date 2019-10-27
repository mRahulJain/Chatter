package com.chatter.chatter.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.Toast
import androidx.core.text.bold
import androidx.core.view.isVisible
import com.chatter.chatter.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_email_login.*

class EmailLoginAct : AppCompatActivity() {

    var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_login)

        var query = SpannableStringBuilder()
            .append("Have query? ")
            .bold {
                append("Click here!")
            }
        queryEmail.text = query
        queryEmail.setOnClickListener {
            var queryString = "Enter your credentials above. \n A verification mail will be sent on your email " +
                    "(Check spam if you don't receive notification).\n" +
                    " Verify that and come back to hit login button for successful creation of your account."
            queryEmail.text = queryString
        }

        sendMail.setOnClickListener {
            if(count == 0) {
                if(emailEL.text.toString() == "" ||
                    passwordEL.text.toString() == "") {
                    Toast.makeText(this,
                        "Enter all credentials",
                        Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                progressEL.isVisible = true
                sendMail.isVisible = false
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailEL.text.toString(),
                    passwordEL.text.toString()).addOnCompleteListener {
                    if(it.isSuccessful) {
                        FirebaseAuth.getInstance().currentUser!!.sendEmailVerification().addOnSuccessListener {
                            sendMail.setText("Login")
                            count = 1
                            Toast.makeText(this,
                                "Email Sent Successfully",
                                Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(this,
                                "Something went wrong",
                                Toast.LENGTH_SHORT).show()
                        }
                        progressEL.isVisible = false
                        sendMail.isVisible = true
                    } else {
                        Toast.makeText(this,
                            "${it.exception!!.localizedMessage}",
                            Toast.LENGTH_SHORT).show()
                        Log.d("myCHECK", "${it.exception!!.localizedMessage}")
                    }
                    progressEL.isVisible = false
                    sendMail.isVisible = true
                }
            } else {
                progressEL.isVisible = true
                sendMail.isVisible = false
                FirebaseAuth.getInstance().signInWithEmailAndPassword(emailEL.text.toString(),
                    passwordEL.text.toString()).addOnCompleteListener {
                    if(it.isSuccessful) {
                        if(FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {
                            val intent = Intent(this, CreateAccountDetailsAct::class.java)
                            intent.putExtra("password", "${passwordEL.text.toString()}")
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this,
                                "Please verify your mail first",
                                Toast.LENGTH_SHORT).show()
                            progressEL.isVisible = false
                            sendMail.isVisible = true
                        }
                    } else {
                        Toast.makeText(this,
                            "${it.exception!!.localizedMessage}",
                            Toast.LENGTH_SHORT).show()
                        progressEL.isVisible = false
                        sendMail.isVisible = true
                    }
                }
            }
        }
    }
}
