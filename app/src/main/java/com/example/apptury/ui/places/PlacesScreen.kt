package com.example.apptury.ui.places

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.apptury.ui.components.CategoryItem
import com.example.apptury.ui.components.LoadingOverlay
import com.example.apptury.ui.components.PlaceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesScreen(
    viewModel: PlacesViewModel,
    onPlaceClick: (Int) -> Unit,
    onNavigateToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    // Load predefined places if needed
    LaunchedEffect(key1 = Unit) {
        if (uiState.places.isEmpty()) {
            viewModel.loadPredefinedPlaces()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Исследуйте") }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Search field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Поиск достопримечательности") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Поиск"
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        singleLine = true
                    )
                    
                    Button(
                        onClick = { 
                            onNavigateToMap()
                        }
                    ) {
                        Text("На карте")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Categories section
                Text(
                    text = "Категории",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                LazyRow(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        CategoryItem(
                            name = "Все",
                            isSelected = uiState.selectedCategory == null,
                            onClick = { viewModel.filterByCategory(null) }
                        )
                    }
                    
                    items(uiState.categories.toList()) { category ->
                        CategoryItem(
                            name = category,
                            isSelected = uiState.selectedCategory == category,
                            onClick = { viewModel.filterByCategory(category) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Popular/Recommended section
                Text(
                    text = "Рекомендуемые места",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (uiState.filteredPlaces.isEmpty() && !uiState.isLoading) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Text(
                            text = "Места не найдены",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val filteredPlaces = if (searchQuery.isBlank()) {
                            uiState.filteredPlaces
                        } else {
                            uiState.filteredPlaces.filter {
                                it.name.contains(searchQuery, ignoreCase = true) ||
                                it.description.contains(searchQuery, ignoreCase = true)
                            }
                        }
                        
                        items(filteredPlaces) { place ->
                            PlaceCard(
                                place = place,
                                onPlaceClick = { onPlaceClick(place.id) },
                                onFavoriteToggle = viewModel::toggleFavorite,
                                onOfflineToggle = viewModel::toggleOfflineAvailability
                            )
                        }
                    }
                }
            }
            
            // Error message
            if (uiState.error != null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = uiState.error ?: "Произошла ошибка",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Loading overlay
            LoadingOverlay(isLoading = uiState.isLoading)
        }
    }
} 