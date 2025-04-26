package com.example.apptury.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.apptury.data.model.Place
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(places: List<Place>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(place: Place): Long

    @Update
    suspend fun update(place: Place)

    @Query("SELECT * FROM places")
    fun getAllPlaces(): Flow<List<Place>>

    @Query("SELECT * FROM places WHERE id = :placeId")
    fun getPlaceById(placeId: Int): Flow<Place>

    @Query("SELECT * FROM places WHERE category = :category")
    fun getPlacesByCategory(category: String): Flow<List<Place>>

    @Query("SELECT * FROM places WHERE isFavorite = 1")
    fun getFavoritePlaces(): Flow<List<Place>>
    
    @Query("UPDATE places SET isFavorite = :isFavorite WHERE id = :placeId")
    suspend fun updateFavoriteStatus(placeId: Int, isFavorite: Boolean)
    
    @Query("UPDATE places SET isOfflineAvailable = :isAvailable WHERE id = :placeId")
    suspend fun updateOfflineAvailability(placeId: Int, isAvailable: Boolean)
    
    @Query("SELECT * FROM places WHERE isOfflineAvailable = 1")
    fun getOfflinePlaces(): Flow<List<Place>>
} 