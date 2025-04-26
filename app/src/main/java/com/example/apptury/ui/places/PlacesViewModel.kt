package com.example.apptury.ui.places

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

data class PlacesUiState(
    val isLoading: Boolean = false,
    val places: List<Place> = emptyList(),
    val filteredPlaces: List<Place> = emptyList(),
    val selectedCategory: String? = null,
    val categories: Set<String> = emptySet(),
    val error: String? = null
)

class PlacesViewModel(private val placeRepository: PlaceRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PlacesUiState(isLoading = true))
    val uiState: StateFlow<PlacesUiState> = _uiState.asStateFlow()
    
    init {
        loadPlaces()
    }
    
    private fun loadPlaces() {
        viewModelScope.launch {
            placeRepository.getAllPlaces()
                .catch { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = e.message
                        ) 
                    }
                }
                .collect { places ->
                    val categories = places.map { it.category }.toSet()
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            places = places,
                            filteredPlaces = places,
                            categories = categories,
                            error = null
                        ) 
                    }
                }
        }
    }
    
    fun filterByCategory(category: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedCategory = category) }
            
            if (category == null) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        filteredPlaces = it.places
                    )
                }
                return@launch
            }
            
            placeRepository.getPlacesByCategory(category)
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
                            filteredPlaces = places,
                            error = null
                        ) 
                    }
                }
        }
    }
    
    fun toggleFavorite(placeId: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            placeRepository.toggleFavoriteStatus(placeId, isFavorite)
            
            // Update the UI state with the new favorite status
            _uiState.update { state ->
                val updatedPlaces = state.places.map { place ->
                    if (place.id == placeId) place.copy(isFavorite = isFavorite) else place
                }
                val updatedFilteredPlaces = state.filteredPlaces.map { place ->
                    if (place.id == placeId) place.copy(isFavorite = isFavorite) else place
                }
                
                state.copy(
                    places = updatedPlaces,
                    filteredPlaces = updatedFilteredPlaces
                )
            }
        }
    }
    
    fun toggleOfflineAvailability(placeId: Int, isAvailable: Boolean) {
        viewModelScope.launch {
            placeRepository.toggleOfflineAvailability(placeId, isAvailable)
            
            // Update the UI state with the new offline availability status
            _uiState.update { state ->
                val updatedPlaces = state.places.map { place ->
                    if (place.id == placeId) place.copy(isOfflineAvailable = isAvailable) else place
                }
                val updatedFilteredPlaces = state.filteredPlaces.map { place ->
                    if (place.id == placeId) place.copy(isOfflineAvailable = isAvailable) else place
                }
                
                state.copy(
                    places = updatedPlaces,
                    filteredPlaces = updatedFilteredPlaces
                )
            }
        }
    }
    
    fun loadPredefinedPlaces() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                placeRepository.loadPredefinedPlaces()
                loadPlaces() // Refresh the list after loading predefined places
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    ) 
                }
            }
        }
    }
} 