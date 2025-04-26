package com.example.apptury.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun AppBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem(
            route = AppScreen.Home.route,
            icon = Icons.Filled.Home,
            label = "Главная"
        ),
        BottomNavItem(
            route = AppScreen.Map.route,
            icon = Icons.Filled.Map,
            label = "Карта"
        ),
        BottomNavItem(
            route = AppScreen.Routes.route,
            icon = Icons.Filled.Route,
            label = "Маршруты"
        ),
        BottomNavItem(
            route = AppScreen.Favorites.route,
            icon = Icons.Filled.Favorite,
            label = "Избранное"
        ),
        BottomNavItem(
            route = AppScreen.Profile.route,
            icon = Icons.Filled.AccountCircle,
            label = "Профиль"
        )
    )
    
    NavigationBar(
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = item.route == currentRoute
            
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = { 
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(text = item.label) }
            )
        }
    }
} 