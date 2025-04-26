package com.example.apptury.ui.map

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.apptury.R
import com.example.apptury.data.model.Place
import com.example.apptury.ui.components.LoadingOverlay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onPlaceClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showMapError by remember { mutableStateOf(false) }
    var filteredPlaces by remember { mutableStateOf<List<Place>>(emptyList()) }
    var showSearchResults by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<Place>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Обновляем отфильтрованные места при изменении поискового запроса или списка мест
    fun updateFilteredPlaces(query: String) {
        if (query.isBlank()) {
            filteredPlaces = uiState.places
            showSearchResults = false
            return
        }
        
        isSearching = true
        
        // Поиск в локальной базе данных
        filteredPlaces = uiState.places.filter { place ->
            place.name.contains(query, ignoreCase = true) ||
            place.description.contains(query, ignoreCase = true) ||
            place.category.contains(query, ignoreCase = true)
        }
        
        // Если найдено мало результатов, добавляем предложения для веб-поиска
        if (filteredPlaces.size < 3) {
            // Создаем специальный объект для веб-поиска
            val webSearchPlace = Place(
                id = -100, // Специальный ID для веб-поиска
                name = "Поиск в интернете: $query",
                description = "Нажмите, чтобы найти больше результатов в интернете",
                category = "Поиск",
                latitude = 0.0,
                longitude = 0.0,
                rating = 0.0F,
                isFavorite = false,
                imageUrl = "",
                isOfflineAvailable = false
            )
            
            searchResults = listOf(webSearchPlace)
            showSearchResults = true
        } else {
            showSearchResults = false
        }
        
        isSearching = false
    }
    
    // Функция для открытия веб-поиска
    fun openWebSearch(query: String) {
        val searchUrl = "https://www.google.com/search?q=$query+достопримечательности+музеи"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl))
        context.startActivity(intent)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Карта") }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Поле поиска с кнопкой
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                shadowElevation = 4.dp,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { 
                                searchQuery = it
                                if (it.isNotBlank()) {
                                    updateFilteredPlaces(it)
                                } else {
                                    filteredPlaces = uiState.places
                                    showSearchResults = false
                                }
                            },
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
                            onClick = { updateFilteredPlaces(searchQuery) }
                        ) {
                            Text("Найти")
                        }
                    }
                    
                    // Показываем результаты поиска
                    if (showSearchResults && searchResults.isNotEmpty()) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            items(searchResults) { place ->
                                SearchResultItem(
                                    place = place,
                                    onClick = {
                                        if (place.id == -100) {
                                            // Это запрос веб-поиска
                                            openWebSearch(searchQuery)
                                        } else {
                                            viewModel.selectPlace(place.id)
                                            showSearchResults = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Инициализируем фильтрованные места при загрузке
            if (filteredPlaces.isEmpty() && uiState.places.isNotEmpty()) {
                filteredPlaces = uiState.places
            }
            
            if (!showMapError) {
                EnhancedOSMMapView(
                    places = filteredPlaces,
                    selectedPlace = uiState.selectedPlace,
                    onPlaceSelected = { viewModel.selectPlace(it) },
                    onFavoriteToggle = viewModel::toggleFavorite,
                    onNavigateToDetail = onPlaceClick,
                    onMapError = { showMapError = true },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = if (showSearchResults) 250.dp else 90.dp) // Оставляем место для поля поиска и результатов
                )
            } else {
                // Если карта не может быть загружена, показываем сообщение об ошибке
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 90.dp, start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Не удалось загрузить карту",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Проверьте подключение к интернету или перезапустите приложение",
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Если найдено несколько мест, показываем количество
            if (filteredPlaces.isNotEmpty() && searchQuery.isNotBlank() && !showSearchResults) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 90.dp, end = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "Найдено: ${filteredPlaces.size}",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Индикатор загрузки
            LoadingOverlay(isLoading = uiState.isLoading || isSearching)
        }
    }
}

@Composable
fun EnhancedOSMMapView(
    places: List<Place>,
    selectedPlace: Place?,
    onPlaceSelected: (Int) -> Unit,
    onFavoriteToggle: (Int, Boolean) -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onMapError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Конфигурация OSMdroid
    try {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidTileCache = File(context.cacheDir, "osm_tiles").apply {
                if (!exists()) {
                    mkdirs()
                }
            }
            osmdroidBasePath = context.cacheDir
        }
    } catch (e: Exception) {
        e.printStackTrace()
        onMapError()
        return
    }
    
    // Создаем и запоминаем MapView
    val mapView = remember {
        try {
            MapView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                minZoomLevel = 4.0
                maxZoomLevel = 19.0
                isTilesScaledToDpi = true  // Улучшаем масштабирование плиток
                isHorizontalMapRepetitionEnabled = false  // Отключаем повторение карты по горизонтали
                isVerticalMapRepetitionEnabled = false    // Отключаем повторение карты по вертикали
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onMapError()
            return@remember MapView(context)
        }
    }
    
    // Наблюдаем за жизненным циклом для запуска/остановки MapView
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            try {
                when (event) {
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    else -> {}
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onMapError()
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            try {
                lifecycleOwner.lifecycle.removeObserver(observer)
                mapView.onDetach()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // Отображаем саму карту
    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { map ->
            try {
                // Очищаем наложения
                map.overlays.clear()
                
                // Добавляем маркеры для всех мест
                places.forEach { place ->
                    try {
                        addCustomMarker(
                            context = context,
                            map = map,
                            place = place,
                            isSelected = place.id == selectedPlace?.id,
                            onMarkerClick = { onPlaceSelected(place.id) }
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                // Если есть выбранное место, показываем дополнительную информацию
                if (selectedPlace != null) {
                    // Центрируем карту на выбранном месте
                    map.controller.apply {
                        setZoom(16.0)
                        setCenter(GeoPoint(selectedPlace.latitude, selectedPlace.longitude))
                    }
                } else {
                    // Если место не выбрано, устанавливаем карту в центр России или на центр найденных мест
                    if (places.isNotEmpty()) {
                        // Вычисляем средние координаты всех мест
                        val avgLat = places.map { it.latitude }.average()
                        val avgLon = places.map { it.longitude }.average()
                        
                        // Определяем границы для лучшего масштабирования
                        val minLat = places.minByOrNull { it.latitude }?.latitude ?: (avgLat - 0.1)
                        val maxLat = places.maxByOrNull { it.latitude }?.latitude ?: (avgLat + 0.1)
                        val minLon = places.minByOrNull { it.longitude }?.longitude ?: (avgLon - 0.1)
                        val maxLon = places.maxByOrNull { it.longitude }?.longitude ?: (avgLon + 0.1)
                        
                        // Создаем ограничивающий прямоугольник
                        val boundingBox = org.osmdroid.util.BoundingBox(
                            maxLat, maxLon, minLat, minLon
                        )
                        
                        // Устанавливаем зум, чтобы видеть все места
                        if (places.size > 1) {
                            map.zoomToBoundingBox(boundingBox, true, 100)
                        } else {
                            map.controller.apply {
                                setZoom(14.0)
                                setCenter(GeoPoint(avgLat, avgLon))
                            }
                        }
                    } else {
                        // Если нет мест, отображаем центр России
                        map.controller.apply {
                            setZoom(4.0)
                            setCenter(GeoPoint(62.0, 94.0)) // Примерный центр России
                        }
                    }
                }
                
                map.invalidate()
            } catch (e: Exception) {
                // Обработать ошибки обновления карты
                e.printStackTrace()
                onMapError()
            }
        }
    )

    // Если есть выбранное место, показываем карточку с информацией
    if (selectedPlace != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            PlaceInfoCard(
                place = selectedPlace,
                onFavoriteToggle = onFavoriteToggle,
                onNavigateToDetail = onNavigateToDetail
            )
        }
    }
}

private fun addCustomMarker(
    context: Context,
    map: MapView,
    place: Place,
    isSelected: Boolean,
    onMarkerClick: () -> Unit
) {
    try {
        val marker = Marker(map).apply {
            position = GeoPoint(place.latitude, place.longitude)
            title = place.name
            snippet = place.category
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            
            // Делаем маркер больше и ярче, если он выбран
            if (isSelected) {
                icon = context.getDrawable(android.R.drawable.ic_menu_compass)
            } else {
                icon = context.getDrawable(android.R.drawable.ic_menu_mylocation)
            }
            
            // Добавляем обработчик клика
            setOnMarkerClickListener { _, _ ->
                onMarkerClick()
                true
            }
            
            // Добавляем информационное окно
          //  setInfoWindow(InfoWindow(R.layout.bonuspack_bubble, map))
        }
        
        map.overlays.add(marker)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun PlaceInfoCard(
    place: Place,
    onFavoriteToggle: (Int, Boolean) -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = { onNavigateToDetail(place.id) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = { onFavoriteToggle(place.id, !place.isFavorite) }
                ) {
                    Icon(
                        imageVector = if (place.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (place.isFavorite) "Удалить из избранного" else "Добавить в избранное",
                        tint = if (place.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Местоположение",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 4.dp)
                )
                
                Text(
                    text = place.category,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = place.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { onNavigateToDetail(place.id) }
                ) {
                    Text("Подробнее")
                }
            }
        }
    }
}

// Компонент для отображения результата поиска
@Composable
fun SearchResultItem(
    place: Place,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (place.id == -100) Icons.Default.Web else Icons.Default.LocationOn,
                contentDescription = "Иконка места",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (place.id != -100) {
                    Text(
                        text = place.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
} 