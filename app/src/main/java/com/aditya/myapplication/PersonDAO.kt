package com.aditya.myapplication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PersonDao {
    @Insert
    suspend fun insert(person: Person): Long

    @Query("SELECT * FROM person_table")
    suspend fun getAllPersons(): List<Person>

    @Query("SELECT COUNT(*) FROM person_table WHERE name = :name")
    suspend fun getPersonCountByName(name: String): Int
}