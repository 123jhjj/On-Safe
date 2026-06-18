package app.skons.onsafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.skons.onsafe.ui.components.AppDrawer
import app.skons.onsafe.ui.screens.AccidentScreen
import app.skons.onsafe.ui.screens.ContactsScreen
import app.skons.onsafe.ui.screens.HomeScreen
import app.skons.onsafe.ui.screens.ReportFlowScreen
import app.skons.onsafe.ui.screens.ScriptScreen
import app.skons.onsafe.ui.screens.SplashScreen
import app.skons.onsafe.ui.theme.OnSafeTheme
import app.skons.onsafe.viewmodel.ContactViewModel
import app.skons.onsafe.viewmodel.LocationViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDark = isSystemInDarkTheme()
            OnSafeTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                val contactViewModel: ContactViewModel = viewModel()
                val locationViewModel: LocationViewModel = viewModel()

                var drawerOpen by remember { mutableStateOf(false) }

                BackHandler(enabled = drawerOpen) { drawerOpen = false }

                Box(Modifier.fillMaxSize()) {
                    NavHost(navController = navController, startDestination = "splash") {
                        composable("splash") {
                            SplashScreen(navController = navController)
                        }
                        composable("home") {
                            HomeScreen(
                                navController = navController,
                                contactViewModel = contactViewModel,
                                locationViewModel = locationViewModel,
                                isDark = isDark,
                                onMenuClick = { drawerOpen = true },
                            )
                        }
                        composable("accident") {
                            AccidentScreen(
                                navController = navController,
                                locationViewModel = locationViewModel,
                                isDark = isDark,
                                onMenuClick = { drawerOpen = true },
                            )
                        }
                        composable("report") {
                            ReportFlowScreen(
                                navController = navController,
                                contactViewModel = contactViewModel,
                                isDark = isDark,
                                onMenuClick = { drawerOpen = true },
                            )
                        }
                        composable("contacts") {
                            ContactsScreen(
                                navController = navController,
                                contactViewModel = contactViewModel,
                                locationViewModel = locationViewModel,
                                isDark = isDark,
                                onMenuClick = { drawerOpen = true },
                            )
                        }
                        composable("script") {
                            ScriptScreen(
                                contactViewModel = contactViewModel,
                                locationViewModel = locationViewModel,
                                isDark = isDark,
                                onBack = { if (navController.previousBackStackEntry != null) navController.popBackStack() },
                                onMenuClick = { drawerOpen = true },
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo("home") { inclusive = false }
                                        launchSingleTop = true
                                    }
                                },
                            )
                        }
                    }

                    if (drawerOpen) {
                        AppDrawer(
                            contactViewModel = contactViewModel,
                            locationViewModel = locationViewModel,
                            isDark = isDark,
                            onDismiss = { drawerOpen = false },
                        )
                    }
                }
            }
        }
    }
}
