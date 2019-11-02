package com.chatter.chatter.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.chatter.chatter.R
import kotlinx.android.synthetic.main.activity_phone_auth.*

class PhoneAuthAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_auth)

        reqOTP.setOnClickListener {
            reqOTP.setOnClickListener {
                if(countryCode.text.toString() == "") {
                    Toast.makeText(this, "Country Code should not be empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if(phoneNumber.text.toString() == "") {
                    Toast.makeText(this, "Phone number should not be empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if(phoneNumber.text.toString().length < 10) {
                    Toast.makeText(this, "Phone number not valid", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val code = countryCode.text.toString()
                val num = phoneNumber.text.toString()
                val number = "+" + code + num

                var intent = Intent(this, OTPAct::class.java)
                intent.putExtra("number", "${num}")
                intent.putExtra("phoneNumber", "${number}")
                startActivity(intent)
            }
        }
    }
}
