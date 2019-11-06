package com.chatter.chatter.Objects_Classes

data class Message(
    val messageNumber : String = "",
    val name : String = "",
    val text : String = "",
    val uid : String = "",
    var toggle : Int = 0,
    var type : String = "",
    val time : String = "",
    val date : String = ""
)