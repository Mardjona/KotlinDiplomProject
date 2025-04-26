package com.example.apptury.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apptury.data.model.Place
import com.example.apptury.data.repository.PlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val isLoading: Boolean = false,
    val favorites: List<Place> = emptyList(),
    val error: String? = null
)

class FavoritesViewModel(private val placeRepository: PlaceRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FavoritesUiState(isLoading = true))
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()
    
    init {
        loadFavorites()
    }
    
    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            placeRepository.getFavoritePlaces()
                .catch { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = e.message
                        ) 
                    }
                }
                .collect { places ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            favorites = places,
                            error = null
                        ) 
                    }
                }
        }
    }
    
    fun removeFromFavorites(placeId: Int) {
        viewModelScope.launch {
            placeRepository.toggleFavoriteStatus(placeId, false)
            
            // Update the UI state without having to reload all favorites
            _uiState.update { state ->
                val updatedFavorites = state.favorites.filter { it.id != placeId }
                state.copy(favorites = updatedFavorites)
            }
        }
    }
    
    fun toggleOfflineAvailability(placeId: Int, isAvailable: Boolean) {
        viewModelScope.launch {
            placeRepository.toggleOfflineAvailability(placeId, isAvailable)
            
            // Update the UI state
            _uiState.update { state ->
                val updatedFavorites = state.favorites.map { place ->
                    if (place.id == placeId) place.copy(isOfflineAvailable = isAvailable) else place
                }
                
                state.copy(favorites = updatedFavorites)
            }
        }
    }
} 