package com.chatter.chatter.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.chatter.chatter.Objects_Classes.MobileCodes
import com.chatter.chatter.R
import kotlinx.android.synthetic.main.activity_create_account.*

class CreateAccountAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        spnCountryCode.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            MobileCodes.countryNames
        )

        google.setOnClickListener {
            val intent = Intent(this, CreateAccountDetailsAct::class.java)
            startActivity(intent)
        }
    }
}
