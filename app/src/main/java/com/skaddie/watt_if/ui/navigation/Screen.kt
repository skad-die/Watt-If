package com.skaddie.watt_if.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object History : Screen("history")
    object Settings : Screen("settings")

    companion object {
        fun fromRoute(route: String?): Screen = when (route) {
            "home" -> Home
            "history" -> History
            "settings" -> Settings
            else -> Home
        }

        fun indexOf(screen: Screen) = when (screen) {
            is Home -> 0
            is History -> 1
            is Settings -> 2
        }
    }
}