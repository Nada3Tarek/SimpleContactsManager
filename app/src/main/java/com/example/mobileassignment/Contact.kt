package com.example.mobileassignment

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true)val ID :Int=0,
    val name: String,
    val phone: String,
    val category: String
){
    override fun toString(): String {
        return "$name - $category"
    }
}
