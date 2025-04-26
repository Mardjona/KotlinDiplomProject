package com.example.apptury

import android.app.Application
import android.content.Context
import com.example.apptury.data.AppContainer
import com.example.apptury.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import java.io.File

class AppturyApplication : Application() {
    lateinit var appContainer: AppContainer
        private set
    
    private val applicationScope = CoroutineScope(Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
        
        // Initialize OSMdroid
        initOSMdroid()
        
        // Create default user
        createDefaultUser()
    }
    
    private fun initOSMdroid() {
        // OSMdroid configuration
        Configuration.getInstance().apply {
            userAgentValue = packageName
            osmdroidTileCache = File(cacheDir, "osm_tiles")
        }
    }
    
    private fun createDefaultUser() {
        applicationScope.launch {
            val userDao = appContainer.userRepository.getUserDao()
            val users = userDao.getAllUsers()
            
            if (users.isEmpty()) {
                // Create a default user if there are no users
                val defaultUser = User(
                    email = "user@example.com",
                    password = "password",
                    username = "Пользователь"
                )
                userDao.insert(defaultUser)
            }
        }
    }
} 