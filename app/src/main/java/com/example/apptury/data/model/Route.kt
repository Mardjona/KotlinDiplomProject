package com.example.apptury.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class Route(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val userId: Int,
    val placeIds: List<Int>,
    val totalDistance: Double,
    val estimatedTime: Int, // in minutes
    val isOfflineAvailable: Boolean = false
) 