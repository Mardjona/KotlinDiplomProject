package com.example.apptury.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.apptury.AppturyApplication
import com.example.apptury.ui.auth.AuthViewModel
import com.example.apptury.ui.favorites.FavoritesViewModel
import com.example.apptury.ui.map.MapViewModel
import com.example.apptury.ui.places.PlacesViewModel
import com.example.apptury.ui.profile.ProfileViewModel
import com.example.apptury.ui.routes.RoutesViewModel

object AppViewModelProvider {
    
    val Factory = viewModelFactory {
        // Auth ViewModel
        initializer {
            AuthViewModel(
                appturyApplication().appContainer.userRepository
            )
        }
        
        // Places ViewModel
        initializer {
            PlacesViewModel(
                appturyApplication().appContainer.placeRepository
            )
        }
        
        // Favorites ViewModel
        initializer {
            FavoritesViewModel(
                appturyApplication().appContainer.placeRepository
            )
        }
        
        // Profile ViewModel
        initializer {
            ProfileViewModel(
                appturyApplication().appContainer.userRepository
            )
        }
        
        // Map ViewModel
        initializer {
            MapViewModel(
                appturyApplication().appContainer.placeRepository
            )
        }
        
        // Routes ViewModel needs user ID - will be created separately
    }
    
    /**
     * Create a RoutesViewModel with the given user ID
     */
    fun createRoutesViewModel(
        application: Application,
        userId: Int
    ): RoutesViewModel {
        val appContainer = (application as AppturyApplication).appContainer
        return RoutesViewModel(
            routeRepository = appContainer.routeRepository,
            placeRepository = appContainer.placeRepository,
            userId = userId
        )
    }
    
    /**
     * Extension function to get Application instance from CreationExtras
     */
    private fun CreationExtras.appturyApplication(): AppturyApplication =
        (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AppturyApplication)
} 