package com.example.apptury.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apptury.data.model.User
import com.example.apptury.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUser: User? = null,
    val error: String? = null
)

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        // Check for saved user session on startup
        checkUserSession()
    }
    
    private fun checkUserSession() {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            userRepository.getUserSession().collect { user ->
                if (user != null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            currentUser = user,
                            error = null
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isLoggedIn = false,
                            currentUser = null
                        )
                    }
                }
            }
        }
    }
    
    fun login(email: String, password: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            userRepository.loginUser(email, password)
                .onSuccess { user ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            currentUser = user,
                            error = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = e.message
                        )
                    }
                }
        }
    }
    
    fun register(email: String, password: String, username: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            userRepository.registerUser(email, password, username)
                .onSuccess { user ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            currentUser = user,
                            error = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = e.message
                        )
                    }
                }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
            _uiState.update { 
                AuthUiState() 
            }
        }
    }
} 