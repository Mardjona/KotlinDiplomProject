package com.example.apptury.ui.routes

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.apptury.data.model.Place
import com.example.apptury.data.model.Route
import com.example.apptury.ui.components.LoadingOverlay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutesScreen(
    viewModel: RoutesViewModel,
    onNavigateToPlaces: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var tabIndex by remember { mutableIntStateOf(0) }
    var showNewRouteDialog by remember { mutableStateOf(false) }
    var startPlace by remember { mutableStateOf<Place?>(null) }
    var endPlace by remember { mutableStateOf<Place?>(null) }
    var routeName by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<Place>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Получаем маршруты при первом запуске
    LaunchedEffect(Unit) {
        // Маршруты уже загружаются в init блоке ViewModel
    }
    
    // Функция для поиска мест
    fun searchPlaces(query: String) {
        if (query.isBlank()) {
            searchResults = emptyList()
            showSearchResults = false
            return
        }
        
        isSearching = true
        
        // Поиск в локальной базе данных
        val results = uiState.availablePlaces.filter { place ->
            place.name.contains(query, ignoreCase = true) ||
            place.description.contains(query, ignoreCase = true) ||
            place.category.contains(query, ignoreCase = true)
        }
        
        // Если найдено мало результатов, добавляем предложения для веб-поиска
        if (results.size < 3) {
            // Создаем специальный объект для веб-поиска
            val webSearchPlace = Place(
                id = -100, // Специальный ID для веб-поиска
                name = "Поиск в интернете: $query",
                description = "Нажмите, чтобы найти больше результатов в интернете",
                category = "Поиск",
                latitude = 0.0,
                longitude = 0.0,
                rating = 0.0f,
                isFavorite = false,
                imageUrl = "",
                isOfflineAvailable = false
            )
            
            searchResults = listOf(webSearchPlace)
        } else {
            searchResults = results
        }
        
        showSearchResults = true
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
                title = { Text("Мои маршруты") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewRouteDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Создать маршрут"
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = tabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = tabIndex == 0,
                    onClick = { tabIndex = 0 },
                    text = { Text("Мои маршруты") },
                    icon = { Icon(Icons.Default.Map, contentDescription = "Мои маршруты") }
                )
                Tab(
                    selected = tabIndex == 1,
                    onClick = { tabIndex = 1 },
                    text = { Text("Построить маршрут") },
                    icon = { Icon(Icons.Default.Place, contentDescription = "Построить маршрут") }
                )
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (tabIndex) {
                    0 -> {
                        // Вкладка со списком маршрутов
                        if (uiState.routes.isEmpty() && !uiState.isLoading) {
                            // Если нет маршрутов, показываем сообщение и кнопку для создания
                            Column(
                                modifier = Modifier
                                    .fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "У вас пока нет сохраненных маршрутов",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "Создайте свой первый маршрут, выбрав интересные места на карте",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Button(onClick = { tabIndex = 1 }) {
                                    Text("Построить маршрут")
                                }
                            }
                        } else if (uiState.error != null) {
                            // Если есть ошибка, показываем сообщение об ошибке
                            Column(
                                modifier = Modifier
                                    .fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Произошла ошибка при загрузке маршрутов",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.error
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = uiState.error ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Button(onClick = { viewModel.loadRoutes() }) {
                                    Text("Повторить попытку")
                                }
                            }
                        } else if (uiState.routes.isNotEmpty()) {
                            // Отображаем список маршрутов
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                uiState.routes.forEach { route ->
                                    RouteCard(
                                        route = route,
                                        onRouteClick = { viewModel.selectRoute(route.id) },
                                        onDeleteClick = { viewModel.deleteRoute(route.id) }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                            
                            // Если выбран маршрут, показываем подробности
                            if (uiState.selectedRoute != null && uiState.selectedRoutePlaces.isNotEmpty()) {
                                RouteDetailDialog(
                                    route = uiState.selectedRoute!!,
                                    places = uiState.selectedRoutePlaces,
                                    onDismiss = { viewModel.selectRoute(-1) }
                                )
                            }
                        }
                    }
                    1 -> {
                        // Вкладка построения маршрута
                        BuildRouteTab(
                            availablePlaces = uiState.availablePlaces,
                            startPlace = startPlace,
                            endPlace = endPlace,
                            routeName = routeName,
                            onRouteNameChange = { routeName = it },
                            onStartPlaceChange = { startPlace = it },
                            onEndPlaceChange = { endPlace = it },
                            onCreateRoute = { name, placeIds, distance, time ->
                                viewModel.createRoute(
                                    name = name,
                                    placeIds = placeIds,
                                    totalDistance = distance,
                                    estimatedTime = time
                                )
                                
                                // Сбрасываем значения
                                routeName = ""
                                startPlace = null
                                endPlace = null
                                
                                // Переключаемся на вкладку с маршрутами
                                tabIndex = 0
                            },
                            searchQuery = searchQuery,
                            onSearchQueryChange = { 
                                searchQuery = it
                                if (it.isNotBlank()) {
                                    searchPlaces(it)
                                } else {
                                    searchResults = emptyList()
                                    showSearchResults = false
                                }
                            },
                            searchResults = searchResults,
                            showSearchResults = showSearchResults,
                            onSearchResultClick = { place ->
                                if (place.id == -100) {
                                    // Это запрос веб-поиска
                                    openWebSearch(searchQuery)
                                } else {
                                    // Выбираем место в зависимости от того, какое поле активно
                                    if (startPlace == null) {
                                        startPlace = place
                                    } else if (endPlace == null) {
                                        endPlace = place
                                    }
                                    showSearchResults = false
                                }
                            },
                            onWebSearchClick = { openWebSearch(searchQuery) }
                        )
                    }
                }
                
                // Индикатор загрузки
                LoadingOverlay(isLoading = uiState.isLoading || isSearching)
            }
        }
    }
}

@Composable
fun RouteCard(
    route: Route,
    onRouteClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onRouteClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = route.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${String.format("%.1f", route.totalDistance)} км",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "${route.estimatedTime} мин",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onRouteClick
                ) {
                    Text("Подробнее")
                }
                
                Button(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            }
        }
    }
}

@Composable
fun RouteDetailDialog(
    route: Route,
    places: List<Place>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(route.name) },
        text = {
            Column {
                Text(
                    text = "Информация о маршруте",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Расстояние: ${String.format("%.1f", route.totalDistance)} км")
                Text("Примерное время: ${route.estimatedTime} мин")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Точки маршрута",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                places.forEachIndexed { index, place ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column {
                            Text(
                                text = place.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = place.category,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    if (index < places.size - 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "↓",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Карта с маршрутом
                if (places.size >= 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        RouteMapPreview(
                            startPlace = places.first(),
                            endPlace = places.last()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
fun PlaceSelector(
    label: String,
    selectedPlace: Place?,
    places: List<Place>,
    onPlaceSelect: (Place) -> Unit,
    isLocationSelector: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Button(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLocationSelector && selectedPlace == null) {
                        "Мое текущее местоположение"
                    } else {
                        selectedPlace?.name ?: "Выберите место"
                    }
                )
                Icon(
                    imageVector = if (label == "Откуда") Icons.Default.MyLocation else Icons.Default.Place,
                    contentDescription = "Иконка местоположения"
                )
            }
        }
        
        if (expanded) {
            AlertDialog(
                onDismissRequest = { expanded = false },
                title = { Text("Выберите место") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (isLocationSelector) {
                            Button(
                                onClick = {
                                    onPlaceSelect(
                                        Place(
                                            id = -1,
                                            name = "Мое текущее местоположение",
                                            description = "Ваше текущее местоположение",
                                            category = "Местоположение",
                                            latitude = 55.7558,  // Приблизительные координаты Москвы
                                            longitude = 37.6173, // как пример текущего местоположения
                                            rating = 0.0f,
                                            isFavorite = false,
                                            imageUrl = "",
                                            isOfflineAvailable = false
                                        )
                                    )
                                    expanded = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = "Текущее местоположение",
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text("Использовать текущее местоположение")
                                }
                            }
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        places.forEach { place ->
                            Button(
                                onClick = {
                                    onPlaceSelect(place)
                                    expanded = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(place.name)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { expanded = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}

@Composable
private fun BuildRouteTab(
    availablePlaces: List<Place>,
    startPlace: Place?,
    endPlace: Place?,
    routeName: String,
    onRouteNameChange: (String) -> Unit,
    onStartPlaceChange: (Place) -> Unit,
    onEndPlaceChange: (Place) -> Unit,
    onCreateRoute: (String, List<Int>, Double, Int) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchResults: List<Place>,
    showSearchResults: Boolean,
    onSearchResultClick: (Place) -> Unit,
    onWebSearchClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Построить новый маршрут",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Поле поиска
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Поиск достопримечательности") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Поиск"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = onWebSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Web,
                            contentDescription = "Поиск в интернете"
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        // Результаты поиска
        if (showSearchResults && searchResults.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(searchResults) { place ->
                        SearchResultItem(
                            place = place,
                            onClick = { onSearchResultClick(place) }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Выберите начальную и конечную точки",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = routeName,
                    onValueChange = onRouteNameChange,
                    label = { Text("Название маршрута") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                PlaceSelector(
                    label = "Откуда",
                    selectedPlace = startPlace,
                    places = availablePlaces,
                    onPlaceSelect = onStartPlaceChange,
                    isLocationSelector = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                PlaceSelector(
                    label = "Куда",
                    selectedPlace = endPlace,
                    places = availablePlaces,
                    onPlaceSelect = onEndPlaceChange
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        if (startPlace != null && endPlace != null && routeName.isNotEmpty()) {
                            val placeIds = listOfNotNull(
                                if (startPlace.id > 0) startPlace.id else null,
                                endPlace.id
                            )
                            val distance = calculateDistance(startPlace, endPlace)
                            val time = calculateTime(distance)
                            
                            onCreateRoute(
                                routeName,
                                placeIds,
                                distance,
                                time
                            )
                        }
                    },
                    enabled = startPlace != null && endPlace != null && routeName.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Создать маршрут")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Отображение карты с маршрутом, если выбраны точки
        if (startPlace != null && endPlace != null) {
            Text(
                text = "Предпросмотр маршрута",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                RouteMapPreview(
                    startPlace = startPlace,
                    endPlace = endPlace
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Информация о маршруте
            val distance = calculateDistance(startPlace, endPlace)
            val time = calculateTime(distance)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Информация о маршруте",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Расстояние:")
                        Text("${String.format("%.1f", distance)} км")
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Примерное время:")
                        Text("$time мин")
                    }
                }
            }
        }
    }
}

@Composable
fun RouteMapPreview(
    startPlace: Place,
    endPlace: Place
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
            }
        } catch (e: Exception) {
            e.printStackTrace()
            MapView(context)
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
        modifier = Modifier.fillMaxSize(),
        update = { map ->
            try {
                // Очищаем наложения
                map.overlays.clear()
                
                // Добавляем маркеры для начальной и конечной точек
                val startMarker = Marker(map).apply {
                    position = GeoPoint(startPlace.latitude, startPlace.longitude)
                    title = startPlace.name
                    snippet = "Начало"
                    icon = context.getDrawable(android.R.drawable.ic_menu_myplaces)
                }
                
                val endMarker = Marker(map).apply {
                    position = GeoPoint(endPlace.latitude, endPlace.longitude)
                    title = endPlace.name
                    snippet = "Конец"
                    icon = context.getDrawable(android.R.drawable.ic_menu_directions)
                }
                
                // Добавляем линию маршрута
                val routeLine = Polyline().apply {
                    addPoint(GeoPoint(startPlace.latitude, startPlace.longitude))
                    addPoint(GeoPoint(endPlace.latitude, endPlace.longitude))
                    outlinePaint.color = android.graphics.Color.BLUE
                    outlinePaint.strokeWidth = 5f
                }
                
                // Добавляем маркеры и линию на карту
                map.overlays.add(routeLine)
                map.overlays.add(startMarker)
                map.overlays.add(endMarker)
                
                // Устанавливаем зум и центр карты, чтобы видеть весь маршрут
                val points = listOf(
                    GeoPoint(startPlace.latitude, startPlace.longitude),
                    GeoPoint(endPlace.latitude, endPlace.longitude)
                )
                
                map.zoomToBoundingBox(
                    org.osmdroid.util.BoundingBox.fromGeoPoints(points),
                    true,
                    50
                )
                
                map.invalidate()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    )
}

// Вспомогательные функции для расчета расстояния и времени
private fun calculateDistance(start: Place, end: Place): Double {
    val earthRadius = 6371.0 // Радиус Земли в километрах
    
    val latDistance = Math.toRadians(end.latitude - start.latitude)
    val lonDistance = Math.toRadians(end.longitude - start.longitude)
    
    val a = sin(latDistance / 2) * sin(latDistance / 2) +
            cos(Math.toRadians(start.latitude)) * cos(Math.toRadians(end.latitude)) *
            sin(lonDistance / 2) * sin(lonDistance / 2)
    
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    
    return earthRadius * c
}

private fun calculateTime(distance: Double): Int {
    // Предполагаем среднюю скорость 5 км/ч при ходьбе
    val averageSpeed = 5.0
    val timeInHours = distance / averageSpeed
    return (timeInHours * 60).toInt() // Переводим в минуты
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