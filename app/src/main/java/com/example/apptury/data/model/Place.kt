package com.example.apptury.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "places")
data class Place(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String? = null,
    val rating: Float = 0f,
    val isFavorite: Boolean = false,
    val isOfflineAvailable: Boolean = false
) 