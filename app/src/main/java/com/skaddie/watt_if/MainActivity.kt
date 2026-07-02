package com.skaddie.watt_if

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.skaddie.watt_if.ui.history.HistoryScreen
import com.skaddie.watt_if.ui.home.HomeScreen
import com.skaddie.watt_if.ui.navigation.Screen
import com.skaddie.watt_if.ui.settings.SettingsScreen
import com.skaddie.watt_if.ui.theme.WattIfDimens
import com.skaddie.watt_if.ui.theme.WattIfTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WattIfTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    WattIfApp()
                }
            }
        }
    }
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

@Composable
fun WattIfApp() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val items = listOf(
        BottomNavItem(Screen.Home, "Home", Icons.Default.Home),
        BottomNavItem(Screen.History, "History", Icons.Default.History),
        BottomNavItem(Screen.Settings, "Settings", Icons.Default.Settings)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { item ->
                    val selected = currentDestination?.hierarchy
                        ?.any { it.route == item.screen.route } == true

                    NavigationBarItem(
                        icon = {
                            NavigationBarItemContent(
                                icon = item.icon,
                                label = item.label,
                                selected = selected
                            )
                        },
                        label = null,
                        selected = selected,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(
                route = Screen.Home.route,
                enterTransition = {
                    val from = Screen.fromRoute(initialState.destination.route)
                    if (Screen.indexOf(from) > Screen.indexOf(Screen.Home)) {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        )
                    } else {
                        fadeIn(animationSpec = tween(300))
                    }
                },
                exitTransition = {
                    val to = Screen.fromRoute(targetState.destination.route)
                    if (Screen.indexOf(to) > Screen.indexOf(Screen.Home)) {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
                    } else {
                        fadeOut(animationSpec = tween(300))
                    }
                }
            ) { HomeScreen() }

            composable(
                route = Screen.History.route,
                enterTransition = {
                    val from = Screen.fromRoute(initialState.destination.route)
                    if (Screen.indexOf(from) < Screen.indexOf(Screen.History)) {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
                    } else {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        )
                    }
                },
                exitTransition = {
                    val to = Screen.fromRoute(targetState.destination.route)
                    if (Screen.indexOf(to) < Screen.indexOf(Screen.History)) {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        )
                    } else {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
                    }
                }
            ) { HistoryScreen() }

            composable(
                route = Screen.Settings.route,
                enterTransition = {
                    val from = Screen.fromRoute(initialState.destination.route)
                    if (Screen.indexOf(from) < Screen.indexOf(Screen.Settings)) {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
                    } else {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        )
                    }
                },
                exitTransition = {
                    val to = Screen.fromRoute(targetState.destination.route)
                    if (Screen.indexOf(to) < Screen.indexOf(Screen.Settings)) {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        )
                    } else {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
                    }
                }
            ) { SettingsScreen() }
        }
    }
}

@Composable
private fun NavigationBarItemContent(
    icon: ImageVector,
    label: String,
    selected: Boolean
) {
    val contentColor = if (selected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(WattIfDimens.NavBarItemSpacing)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(WattIfDimens.NavIconSize)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
        Box(
            modifier = Modifier
                .width(WattIfDimens.NavIndicatorWidth)
                .height(WattIfDimens.NavIndicatorHeight)
                .clip(RoundedCornerShape(WattIfDimens.NavIndicatorCornerRadius))
                .background(
                    if (selected) MaterialTheme.colorScheme.primary
                    else Color.Transparent
                )
        )
    }
}