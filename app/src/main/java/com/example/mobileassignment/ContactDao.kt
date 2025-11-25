package com.example.mobileassignment

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ContactDao {
    @Insert
    suspend fun insertContact(contact: Contact)

    @Query("SELECT* FROM contacts")
    suspend fun getAllContacts(): List<Contact>

    @Query("SELECT * FROM contacts WHERE category = :category")
    suspend fun getContactsByCategory(category: String): List<Contact>
}