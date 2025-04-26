package com.example.apptury.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.apptury.data.database.dao.UserDao
import com.example.apptury.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserRepository(
    private val userDao: UserDao,
    private val context: Context? = null
) {
    
    // DataStore keys
    private val USER_ID_KEY = intPreferencesKey("user_id")
    private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    private val USER_NAME_KEY = stringPreferencesKey("user_name")
    
    // Expose UserDao for creating default user
    fun getUserDao(): UserDao {
        return userDao
    }
    
    suspend fun registerUser(email: String, password: String, username: String): Result<User> {
        val existingUser = userDao.getUserByEmail(email)
        if (existingUser != null) {
            return Result.failure(Exception("Пользователь с таким email уже существует"))
        }
        
        val user = User(
            email = email,
            password = password,
            username = username
        )
        
        val id = userDao.insert(user)
        val newUser = user.copy(id = id.toInt())
        
        // Save user credentials to DataStore after successful registration
        context?.let { ctx ->
            saveUserSession(ctx, newUser)
        }
        
        return if (id > 0) {
            Result.success(newUser)
        } else {
            Result.failure(Exception("Не удалось зарегистрировать пользователя"))
        }
    }
    
    suspend fun loginUser(email: String, password: String): Result<User> {
        val user = userDao.login(email, password)
        
        // Save user credentials to DataStore after successful login
        if (user != null) {
            context?.let { ctx ->
                saveUserSession(ctx, user)
            }
        }
        
        return if (user != null) {
            Result.success(user)
        } else {
            Result.failure(Exception("Неверный email или пароль"))
        }
    }
    
    private suspend fun saveUserSession(context: Context, user: User) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = user.id
            preferences[USER_EMAIL_KEY] = user.email
            preferences[USER_NAME_KEY] = user.username
        }
    }
    
    suspend fun getUserSession(): Flow<User?> {
        return context?.dataStore?.data?.map { preferences ->
            val userId = preferences[USER_ID_KEY]
            val userEmail = preferences[USER_EMAIL_KEY]
            val userName = preferences[USER_NAME_KEY]
            
            if (userId != null && userEmail != null && userName != null) {
                User(
                    id = userId,
                    email = userEmail,
                    username = userName,
                    password = "" // We don't store password in preferences
                )
            } else {
                null
            }
        } ?: kotlinx.coroutines.flow.flowOf(null)
    }
    
    suspend fun getCurrentUser(): User? {
        return context?.let {
            try {
                val preferences = it.dataStore.data.first()
                val userId = preferences[USER_ID_KEY]
                val userEmail = preferences[USER_EMAIL_KEY]
                val userName = preferences[USER_NAME_KEY]
                
                if (userId != null && userEmail != null && userName != null) {
                    User(
                        id = userId,
                        email = userEmail,
                        username = userName,
                        password = "" // We don't store password in preferences
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    suspend fun logout() {
        context?.dataStore?.edit { preferences ->
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(USER_NAME_KEY)
        }
    }
    
    fun getUserById(userId: Int): Flow<User> {
        return userDao.getUserById(userId)
    }
    
    suspend fun updateUserPreferences(userId: Int, preferences: String) {
        val user = userDao.getUserById(userId).first()
        userDao.update(user.copy(preferences = preferences))
    }
} 