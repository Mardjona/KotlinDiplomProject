package com.example.apptury.ui.map

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

data class MapUiState(
    val isLoading: Boolean = false,
    val places: List<Place> = emptyList(),
    val selectedPlace: Place? = null,
    val error: String? = null
)

class MapViewModel(private val placeRepository: PlaceRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MapUiState(isLoading = true))
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
    
    init {
        loadAllPlaces()
    }
    
    private fun loadAllPlaces() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
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
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            places = places,
                            error = null
                        ) 
                    }
                }
        }
    }
    
    fun selectPlace(placeId: Int) {
        val place = _uiState.value.places.find { it.id == placeId }
        _uiState.update { it.copy(selectedPlace = place) }
    }
    
    fun clearSelectedPlace() {
        _uiState.update { it.copy(selectedPlace = null) }
    }
    
    fun toggleFavorite(placeId: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            placeRepository.toggleFavoriteStatus(placeId, isFavorite)
            
            // Обновляем состояние UI
            _uiState.update { state ->
                val updatedPlaces = state.places.map { place ->
                    if (place.id == placeId) place.copy(isFavorite = isFavorite) else place
                }
                
                val updatedSelectedPlace = state.selectedPlace?.let { selected ->
                    if (selected.id == placeId) selected.copy(isFavorite = isFavorite) else selected
                }
                
                state.copy(
                    places = updatedPlaces,
                    selectedPlace = updatedSelectedPlace
                )
            }
        }
    }
} 