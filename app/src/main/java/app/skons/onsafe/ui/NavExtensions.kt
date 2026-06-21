package app.skons.onsafe.ui

import androidx.navigation.NavHostController

fun NavHostController.navigateMain(route: String) {
    navigate(route) {
        popUpTo("home") { inclusive = false }
        launchSingleTop = true
    }
}
