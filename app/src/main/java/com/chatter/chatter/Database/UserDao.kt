package com.chatter.chatter.Database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface UserDao {
    @Insert
    fun insertRow(user: User)

    @Query("Select * from User")
    fun getUser() : User

    @Query("Delete from User")
    fun deleteUser()
}