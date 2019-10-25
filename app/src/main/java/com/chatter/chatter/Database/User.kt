package com.chatter.chatter.Database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey(autoGenerate = true)
    val id : Long?=null,
    val fullName : String,
    val username : String,
    val dob : String,
    val password : String,
    val gender : String,
    val uid : String
)