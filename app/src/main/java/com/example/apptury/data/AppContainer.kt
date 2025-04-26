package com.example.apptury.data

import android.content.Context
import com.example.apptury.data.database.AppDatabase
import com.example.apptury.data.repository.PlaceRepository
import com.example.apptury.data.repository.RouteRepository
import com.example.apptury.data.repository.UserRepository

class AppContainer(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    
    val userRepository by lazy {
        UserRepository(database.userDao(), context)
    }
    
    val placeRepository by lazy {
        PlaceRepository(database.placeDao())
    }
    
    val routeRepository by lazy {
        RouteRepository(database.routeDao())
    }
} 