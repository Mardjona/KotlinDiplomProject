package com.example.apptury.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.apptury.data.model.Route
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(route: Route): Long

    @Update
    suspend fun update(route: Route)

    @Query("SELECT * FROM routes WHERE userId = :userId")
    fun getRoutesByUserId(userId: Int): Flow<List<Route>>

    @Query("SELECT * FROM routes WHERE id = :routeId")
    fun getRouteById(routeId: Int): Flow<Route>
    
    @Query("DELETE FROM routes WHERE id = :routeId")
    suspend fun deleteRoute(routeId: Int)
    
    @Query("UPDATE routes SET isOfflineAvailable = :isAvailable WHERE id = :routeId")
    suspend fun updateOfflineAvailability(routeId: Int, isAvailable: Boolean)
    
    @Query("SELECT * FROM routes WHERE isOfflineAvailable = 1")
    fun getOfflineRoutes(): Flow<List<Route>>
} 