package com.kampplus.hava.ui

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.city.CityRoute
import com.kampplus.hava.ui.city.FavoritesScreen
import com.kampplus.hava.ui.forecast.ForecastRoute
import com.kampplus.hava.ui.forecast.ForecastViewModel
import com.kampplus.hava.ui.map.TurkeyMapScreen
import com.kampplus.hava.ui.route.RouteAdviceRoute

@Composable
fun HavaNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    favoriteCities: List<City> = emptyList(),
    onToggleFavorite: (City) -> Unit = {},
    onToggleTheme: () -> Unit = {}
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute == CITY_ROUTE ||
        currentRoute == FAVORITES_ROUTE || currentRoute == ROUTE_ADVICE_ROUTE || currentRoute == MAP_ROUTE

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            if (showBottomBar) {
                Column {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tonalElevation = 0.dp
                    ) {
                        listOf(
                            BottomDestination(CITY_ROUTE, "⌕", "Ara"),
                            BottomDestination(MAP_ROUTE, "🗺", "Harita"),
                            BottomDestination(ROUTE_ADVICE_ROUTE, "☂", "Yol"),
                            BottomDestination(FAVORITES_ROUTE, "★", "Şehirlerim")
                        ).forEach { destination ->
                            val isSelected = currentRoute == destination.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (currentRoute != destination.route) {
                                        navController.navigate(destination.route) {
                                            popUpTo(CITY_ROUTE) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Text(
                                        text = destination.icon,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                },
                                label = {
                                    Text(
                                        text = destination.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onSurface,
                                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    indicatorColor = MaterialTheme.colorScheme.surfaceContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = CITY_ROUTE,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(CITY_ROUTE) {
                CityRoute(
                    onCityClick = { city -> navController.navigate(forecastRoute(city)) },
                    favoriteCities = favoriteCities,
                    onToggleFavorite = onToggleFavorite,
                    onOpenFavorites = { navController.navigate(FAVORITES_ROUTE) },
                    onToggleTheme = onToggleTheme
                )
            }
            composable(MAP_ROUTE) {
                TurkeyMapScreen()
            }
            composable(ROUTE_ADVICE_ROUTE) {
                RouteAdviceRoute(
                    favoriteCities = favoriteCities,
                    onOpenSearch = {
                        navController.navigate(CITY_ROUTE) {
                            popUpTo(CITY_ROUTE) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(FAVORITES_ROUTE) {
                FavoritesScreen(
                    cities = favoriteCities,
                    onCityClick = { city -> navController.navigate(forecastRoute(city)) },
                    onRemoveFavorite = onToggleFavorite,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = FORECAST_ROUTE,
                arguments = listOf(
                    navArgument(ForecastViewModel.ARG_NAME) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(ForecastViewModel.ARG_SUBTITLE) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(ForecastViewModel.ARG_LATITUDE) {
                        type = NavType.StringType
                        defaultValue = "0"
                    },
                    navArgument(ForecastViewModel.ARG_LONGITUDE) {
                        type = NavType.StringType
                        defaultValue = "0"
                    }
                )
            ) {
                ForecastRoute(onBack = { navController.popBackStack() })
            }
        }
    }
}

private data class BottomDestination(val route: String, val icon: String, val label: String)

private const val CITY_ROUTE = "city"
private const val MAP_ROUTE = "turkey-map"
private const val FAVORITES_ROUTE = "favorites"
private const val ROUTE_ADVICE_ROUTE = "route-advice"
private const val FORECAST_ROUTE =
    "forecast?name={name}&subtitle={subtitle}&latitude={latitude}&longitude={longitude}"

private fun forecastRoute(city: City): String = buildString {
    append("forecast")
    append("?name=").append(Uri.encode(city.name))
    append("&subtitle=").append(Uri.encode(city.subtitle))
    append("&latitude=").append(city.latitude)
    append("&longitude=").append(city.longitude)
}
