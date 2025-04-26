package com.example.apptury.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.apptury.ui.AppViewModelProvider
import com.example.apptury.ui.auth.AuthViewModel
import com.example.apptury.ui.auth.LoginScreen
import com.example.apptury.ui.auth.RegisterScreen
import com.example.apptury.ui.favorites.FavoritesScreen
import com.example.apptury.ui.favorites.FavoritesViewModel
import com.example.apptury.ui.map.MapScreen
import com.example.apptury.ui.map.MapViewModel
import com.example.apptury.ui.places.PlaceDetailScreen
import com.example.apptury.ui.places.PlacesScreen
import com.example.apptury.ui.places.PlacesViewModel
import com.example.apptury.ui.profile.ProfileScreen
import com.example.apptury.ui.profile.ProfileViewModel
import com.example.apptury.ui.routes.RoutesScreen
import com.example.apptury.ui.routes.RoutesViewModel

enum class AppScreen(val route: String) {
    Login("login"),
    Register("register"),
    Home("home"),
    PlaceDetail("place_detail/{placeId}"),
    Favorites("favorites"),
    Map("map"),
    Routes("routes"),
    Profile("profile")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppScreen.Login.route
) {
    val authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val authState by authViewModel.uiState.collectAsState()
    
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: AppScreen.Login.route
    
    // Auto-login: If user is logged in, navigate to Home
    LaunchedEffect(key1 = authState.isLoggedIn) {
        if (authState.isLoggedIn && currentRoute == AppScreen.Login.route) {
            navController.navigate(AppScreen.Home.route) {
                popUpTo(AppScreen.Login.route) { inclusive = true }
            }
        }
    }
    
    val showBottomBar = currentRoute !in listOf(
        AppScreen.Login.route,
        AppScreen.Register.route
    ) && !currentRoute.startsWith("place_detail")
    
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(AppScreen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = AppScreen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(AppScreen.Home.route) {
                            popUpTo(AppScreen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(AppScreen.Register.route)
                    }
                )
            }
            
            composable(route = AppScreen.Register.route) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onRegisterSuccess = {
                        navController.navigate(AppScreen.Home.route) {
                            popUpTo(AppScreen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(route = AppScreen.Home.route) {
                val placesViewModel: PlacesViewModel = viewModel(factory = AppViewModelProvider.Factory)
                PlacesScreen(
                    viewModel = placesViewModel,
                    onPlaceClick = { placeId ->
                        navController.navigate("place_detail/$placeId")
                    },
                    onNavigateToMap = {
                        navController.navigate(AppScreen.Map.route)
                    }
                )
            }
            
            composable(
                route = "place_detail/{placeId}",
                arguments = listOf(
                    navArgument("placeId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val placeId = backStackEntry.arguments?.getInt("placeId") ?: 0
                val placesViewModel: PlacesViewModel = viewModel(factory = AppViewModelProvider.Factory)
                
                PlaceDetailScreen(
                    placeId = placeId,
                    viewModel = placesViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(route = AppScreen.Favorites.route) {
                val favoritesViewModel: FavoritesViewModel = viewModel(factory = AppViewModelProvider.Factory)
                
                FavoritesScreen(
                    viewModel = favoritesViewModel,
                    onPlaceClick = { placeId ->
                        navController.navigate("place_detail/$placeId")
                    }
                )
            }
            
            composable(route = AppScreen.Map.route) {
                val mapViewModel: MapViewModel = viewModel(factory = AppViewModelProvider.Factory)
                
                MapScreen(
                    viewModel = mapViewModel,
                    onPlaceClick = { placeId ->
                        navController.navigate("place_detail/$placeId")
                    }
                )
            }
            
            composable(route = AppScreen.Routes.route) {
                val routesViewModel: RoutesViewModel = viewModel(factory = AppViewModelProvider.Factory)
                
                RoutesScreen(
                    viewModel = routesViewModel,
                    onNavigateToPlaces = {
                        navController.navigate(AppScreen.Home.route)
                    }
                )
            }
            
            composable(route = AppScreen.Profile.route) {
                val profileViewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
                
                ProfileScreen(
                    viewModel = profileViewModel,
                    onLogout = {
                        // Вызываем logout для AuthViewModel, чтобы обновить состояние авторизации
                        authViewModel.logout()
                        
                        // Переходим на экран входа
                        navController.navigate(AppScreen.Login.route) {
                            popUpTo(AppScreen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
} 