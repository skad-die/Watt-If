package com.skaddie.watt_if.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Result : Screen("result")
    object History : Screen("history")
    object Settings : Screen("settings")
}