package com.example.apptury.data.repository

import com.example.apptury.data.database.dao.PlaceDao
import com.example.apptury.data.model.Place
import kotlinx.coroutines.flow.Flow

class PlaceRepository(private val placeDao: PlaceDao) {
    
    fun getAllPlaces(): Flow<List<Place>> {
        return placeDao.getAllPlaces()
    }
    
    fun getPlacesByCategory(category: String): Flow<List<Place>> {
        return placeDao.getPlacesByCategory(category)
    }
    
    fun getPlaceById(placeId: Int): Flow<Place> {
        return placeDao.getPlaceById(placeId)
    }
    
    fun getFavoritePlaces(): Flow<List<Place>> {
        return placeDao.getFavoritePlaces()
    }
    
    fun getOfflinePlaces(): Flow<List<Place>> {
        return placeDao.getOfflinePlaces()
    }
    
    suspend fun toggleFavoriteStatus(placeId: Int, isFavorite: Boolean) {
        placeDao.updateFavoriteStatus(placeId, isFavorite)
    }
    
    suspend fun toggleOfflineAvailability(placeId: Int, isAvailable: Boolean) {
        placeDao.updateOfflineAvailability(placeId, isAvailable)
    }
    
    suspend fun insertPlaces(places: List<Place>) {
        placeDao.insertAll(places)
    }
    
    // В реальном приложении здесь будет код для загрузки данных из API
    suspend fun loadPredefinedPlaces() {
        val predefinedPlaces = listOf(
            Place(
                name = "Эрмитаж",
                description = "Один из крупнейших художественных музеев мира, расположенный в Санкт-Петербурге",
                category = "Музеи",
                latitude = 59.9398,
                longitude = 30.3146,
                imageUrl = "https://example.com/hermitage.jpg",
                rating = 4.8f
            ),
            Place(
                name = "Красная площадь",
                description = "Главная площадь Москвы, расположенная в центре города",
                category = "Достопримечательности",
                latitude = 55.7539,
                longitude = 37.6208,
                imageUrl = "https://example.com/red_square.jpg",
                rating = 4.7f
            ),
            Place(
                name = "Парк Горького",
                description = "Центральный парк культуры и отдыха имени Горького в Москве",
                category = "Парки",
                latitude = 55.7308,
                longitude = 37.6031,
                imageUrl = "https://example.com/gorky_park.jpg",
                rating = 4.6f
            ),
            Place(
                name = "Большой театр",
                description = "Один из крупнейших в России и один из самых значительных в мире театров оперы и балета",
                category = "Театры",
                latitude = 55.7602,
                longitude = 37.6186,
                imageUrl = "https://example.com/bolshoi.jpg",
                rating = 4.9f
            ),
            Place(
                name = "Третьяковская галерея",
                description = "Художественный музей в Москве, основанный купцом Павлом Третьяковым",
                category = "Музеи",
                latitude = 55.7415,
                longitude = 37.6208,
                imageUrl = "https://example.com/tretyakov.jpg",
                rating = 4.7f
            )
        )
        
        placeDao.insertAll(predefinedPlaces)
    }
} 