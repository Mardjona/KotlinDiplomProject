package com.example.apptury.ui.places

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.apptury.data.model.Place
import com.example.apptury.ui.components.LoadingOverlay
import com.example.apptury.ui.components.OSMMapView
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    placeId: Int,
    viewModel: PlacesViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var place by remember { mutableStateOf<Place?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load the place details
    LaunchedEffect(placeId) {
        isLoading = true
        val allPlaces = viewModel.uiState.first().places
        place = allPlaces.find { it.id == placeId }
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(place?.name ?: "Детали места") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    // Offline toggle
                    IconButton(
                        onClick = { 
                            place?.let { 
                                viewModel.toggleOfflineAvailability(it.id, !it.isOfflineAvailable)
                                place = place?.copy(isOfflineAvailable = !it.isOfflineAvailable)
                            } 
                        }
                    ) {
                        Icon(
                            imageVector = if (place?.isOfflineAvailable == true) {
                                Icons.Outlined.DownloadDone
                            } else {
                                Icons.Outlined.Download
                            },
                            contentDescription = if (place?.isOfflineAvailable == true) {
                                "Удалить из оффлайн"
                            } else {
                                "Сохранить для оффлайн"
                            }
                        )
                    }
                    
                    // Favorite toggle
                    IconButton(
                        onClick = { 
                            place?.let { 
                                viewModel.toggleFavorite(it.id, !it.isFavorite)
                                place = place?.copy(isFavorite = !it.isFavorite)
                            } 
                        }
                    ) {
                        Icon(
                            imageVector = if (place?.isFavorite == true) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Filled.FavoriteBorder
                            },
                            contentDescription = if (place?.isFavorite == true) {
                                "Удалить из избранного"
                            } else {
                                "Добавить в избранное"
                            },
                            tint = if (place?.isFavorite == true) Color.Red else Color.Gray
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        place?.let { currentPlace ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Image
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(currentPlace.imageUrl ?: "https://via.placeholder.com/400x200?text=Нет+изображения")
                        .crossfade(true)
                        .build(),
                    contentDescription = currentPlace.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )
                
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Category badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                com.example.apptury.ui.components.getCategoryColor(currentPlace.category)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = currentPlace.category,
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Name and rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = currentPlace.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Рейтинг",
                                tint = Color.Yellow,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentPlace.rating.toString(),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Location
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Местоположение",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${currentPlace.latitude}, ${currentPlace.longitude}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Description
                    Text(
                        text = "Описание",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = currentPlace.description,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Justify
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Map
                    Text(
                        text = "Местоположение",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // OpenStreetMap
                    OSMMapView(
                        latitude = currentPlace.latitude,
                        longitude = currentPlace.longitude,
                        title = currentPlace.name,
                        zoomLevel = 15.0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Add to route button
                    Button(
                        onClick = { /* Will be implemented in routes feature */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Добавить в маршрут")
                    }
                }
            }
        } ?: run {
            // Loading or place not found
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isLoading) {
                    LoadingOverlay(isLoading = true)
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Место не найдено",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(onClick = onNavigateBack) {
                            Text("Вернуться назад")
                        }
                    }
                }
            }
        }
    }
} 