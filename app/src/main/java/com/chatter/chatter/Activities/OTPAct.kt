package com.chatter.chatter.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import com.chatter.chatter.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_otp.*
import java.util.concurrent.TimeUnit

class OTPAct : AppCompatActivity() {

    lateinit var verificationId : String
    val auth = FirebaseAuth.getInstance()
    var phoneNumber : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        phoneNumber = intent.getStringExtra("phoneNumber")

        greetings.text = "Hey!"

        val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential?) {
                otp!!.setValue(p0!!.smsCode.toString())
            }

            override fun onVerificationFailed(p0: FirebaseException?) {
                Toast.makeText(this@OTPAct, "${p0!!.localizedMessage}", Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(p0: String?, p1: PhoneAuthProvider.ForceResendingToken?) {
                super.onCodeSent(p0, p1)
                verificationId = p0.toString()
            }

        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
            TimeUnit.SECONDS,
            this,
            callback
        )

        proceed.setOnClickListener {
            if(otp.value.toString() == "") {
                Toast.makeText(this@OTPAct, "Enter OTP first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            proceed.isVisible = false
            progressProceed.isVisible = true
            val credential = PhoneAuthProvider.getCredential(verificationId, otp.value.toString())
            signInWithCredential(credential)
//            val intent = Intent(this, CreateAccountDetailsAct::class.java)
//            startActivity(intent)
//            finish()
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential?) {
        auth.signInWithCredential(credential as AuthCredential)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    val intent = Intent(this, CreateAccountDetailsAct::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "${it.exception!!.localizedMessage}", Toast.LENGTH_LONG).show()
                    progressProceed.isVisible = false
                    proceed.isVisible = true
                }
            }
    }
}
