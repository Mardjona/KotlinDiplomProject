package com.example.apptury.data.repository

import com.example.apptury.data.database.dao.RouteDao
import com.example.apptury.data.model.Route
import kotlinx.coroutines.flow.Flow

class RouteRepository(private val routeDao: RouteDao) {
    
    fun getUserRoutes(userId: Int): Flow<List<Route>> {
        return routeDao.getRoutesByUserId(userId)
    }
    
    fun getRouteById(routeId: Int): Flow<Route> {
        return routeDao.getRouteById(routeId)
    }
    
    fun getOfflineRoutes(): Flow<List<Route>> {
        return routeDao.getOfflineRoutes()
    }
    
    suspend fun createRoute(
        name: String,
        userId: Int,
        placeIds: List<Int>,
        totalDistance: Double,
        estimatedTime: Int
    ): Long {
        val route = Route(
            name = name,
            userId = userId,
            placeIds = placeIds,
            totalDistance = totalDistance,
            estimatedTime = estimatedTime
        )
        return routeDao.insert(route)
    }
    
    suspend fun updateRoute(route: Route) {
        routeDao.update(route)
    }
    
    suspend fun deleteRoute(routeId: Int) {
        routeDao.deleteRoute(routeId)
    }
    
    suspend fun toggleOfflineAvailability(routeId: Int, isAvailable: Boolean) {
        routeDao.updateOfflineAvailability(routeId, isAvailable)
    }
} 