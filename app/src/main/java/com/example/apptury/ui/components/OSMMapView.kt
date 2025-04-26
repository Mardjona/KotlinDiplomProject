package com.example.apptury.ui.components

import android.content.Context
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File

@Composable
fun OSMMapView(
    latitude: Double,
    longitude: Double,
    title: String,
    zoomLevel: Double = 15.0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Configure the osmdroid library - ensure it's done safely
    try {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidTileCache = File(context.cacheDir, "osm_tiles").apply {
                if (!exists()) {
                    mkdirs()
                }
            }
            // Не требуется API ключ для OSM
            // Установка настроек для работы без интернета (если кэш есть)
            osmdroidBasePath = context.cacheDir
        }
    } catch (e: Exception) {
        // Обработать ошибку инициализации
        e.printStackTrace()
    }
    
    // Create and remember the MapView
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
            // Обработать ошибку создания карты
            e.printStackTrace()
            MapView(context)
        }
    }
    
    // Observe lifecycle to start/stop the MapView when needed
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            try {
                when (event) {
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    else -> {}
                }
            } catch (e: Exception) {
                // Обработать ошибки жизненного цикла
                e.printStackTrace()
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            try {
                lifecycleOwner.lifecycle.removeObserver(observer)
                // Очистить ресурсы карты
                mapView.onDetach()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // The actual map view
    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { map ->
            try {
                // Setup the map properties
                map.controller.apply {
                    setZoom(zoomLevel)
                    animateTo(GeoPoint(latitude, longitude))
                }
                
                // Add a marker at the location
                createMarker(context, map, latitude, longitude, title)
            } catch (e: Exception) {
                // Обработать ошибки обновления карты
                e.printStackTrace()
            }
        }
    )
}

private fun createMarker(
    context: Context,
    map: MapView,
    latitude: Double,
    longitude: Double,
    title: String
) {
    try {
        // Clear existing overlays
        map.overlays.clear()
        
        // Create a new marker
        val marker = Marker(map).apply {
            position = GeoPoint(latitude, longitude)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            this.title = title
            
            // Используем стандартную иконку Android, если у нас нет своей
            icon = context.getDrawable(android.R.drawable.ic_menu_mylocation)
        }
        
        // Add the marker to the map
        map.overlays.add(marker)
        map.invalidate()
    } catch (e: Exception) {
        // Обработать ошибки создания маркера
        e.printStackTrace()
    }
} 