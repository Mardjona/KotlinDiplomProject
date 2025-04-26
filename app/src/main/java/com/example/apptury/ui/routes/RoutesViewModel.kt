package com.example.apptury.ui.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apptury.data.model.Place
import com.example.apptury.data.model.Route
import com.example.apptury.data.repository.PlaceRepository
import com.example.apptury.data.repository.RouteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RoutesUiState(
    val isLoading: Boolean = false,
    val routes: List<Route> = emptyList(),
    val selectedRouteId: Int? = null,
    val selectedRoute: Route? = null,
    val selectedRoutePlaces: List<Place> = emptyList(),
    val availablePlaces: List<Place> = emptyList(),
    val error: String? = null
)

class RoutesViewModel(
    private val routeRepository: RouteRepository,
    private val placeRepository: PlaceRepository,
    private val userId: Int
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RoutesUiState(isLoading = true))
    val uiState: StateFlow<RoutesUiState> = _uiState.asStateFlow()
    
    init {
        loadRoutes()
        loadAllPlaces()
    }
    
    private fun loadAllPlaces() {
        viewModelScope.launch {
            try {
                placeRepository.getAllPlaces()
                    .collect { places ->
                        _uiState.update { it.copy(availablePlaces = places) }
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Ошибка загрузки мест: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    fun loadRoutes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            routeRepository.getUserRoutes(userId)
                .catch { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = e.message
                        ) 
                    }
                }
                .collect { routes ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            routes = routes,
                            error = null
                        ) 
                    }
                }
        }
    }
    
    fun selectRoute(routeId: Int) {
        viewModelScope.launch {
            if (routeId <= 0) {
                // Сбрасываем выбранный маршрут
                _uiState.update { 
                    it.copy(
                        selectedRouteId = null,
                        selectedRoute = null,
                        selectedRoutePlaces = emptyList()
                    ) 
                }
                return@launch
            }
            
            _uiState.update { it.copy(isLoading = true, selectedRouteId = routeId) }
            
            try {
                val route = routeRepository.getRouteById(routeId).first()
                val allPlaces = placeRepository.getAllPlaces().first()
                val routePlaces = allPlaces.filter { place -> route.placeIds.contains(place.id) }
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedRoute = route,
                        selectedRoutePlaces = routePlaces,
                        error = null
                    )
                }
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
    
    fun createRoute(name: String, placeIds: List<Int>, totalDistance: Double, estimatedTime: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val routeId = routeRepository.createRoute(
                    name = name,
                    userId = userId,
                    placeIds = placeIds,
                    totalDistance = totalDistance,
                    estimatedTime = estimatedTime
                )
                
                loadRoutes()
                if (routeId > 0) {
                    selectRoute(routeId.toInt())
                }
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
    
    fun deleteRoute(routeId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                routeRepository.deleteRoute(routeId)
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedRouteId = null,
                        selectedRoute = null,
                        selectedRoutePlaces = emptyList()
                    )
                }
                
                loadRoutes()
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
    
    fun toggleOfflineAvailability(routeId: Int, isAvailable: Boolean) {
        viewModelScope.launch {
            routeRepository.toggleOfflineAvailability(routeId, isAvailable)
            
            // Update the places in the route for offline use as well
            val routePlaces = _uiState.value.selectedRoutePlaces
            routePlaces.forEach { place ->
                placeRepository.toggleOfflineAvailability(place.id, isAvailable)
            }
            
            // Update the UI state
            _uiState.update { state ->
                val updatedRoutes = state.routes.map { route ->
                    if (route.id == routeId) route.copy(isOfflineAvailable = isAvailable) else route
                }
                
                val updatedSelectedRoute = state.selectedRoute?.let { route ->
                    if (route.id == routeId) route.copy(isOfflineAvailable = isAvailable) else route
                }
                
                state.copy(
                    routes = updatedRoutes,
                    selectedRoute = updatedSelectedRoute
                )
            }
        }
    }
} 