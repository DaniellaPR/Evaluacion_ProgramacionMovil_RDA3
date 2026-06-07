package com.app.apuntes.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.app.apuntes.presentation.screens.ApuntesScreen
import com.app.apuntes.presentation.screens.CrearApunteScreen
import com.app.apuntes.presentation.screens.CrearMateriaScreen
import com.app.apuntes.presentation.screens.DashboardScreen
import com.app.apuntes.presentation.screens.DetalleApunteScreen
import com.app.apuntes.presentation.screens.HorarioScreen
import com.app.apuntes.presentation.screens.PerfilScreen
import com.app.apuntes.presentation.screens.QRScreen
import com.app.apuntes.presentation.screens.ScannerScreen
import kotlin.reflect.KClass

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Any,
    val routeClass: KClass<*>
)

private val bottomNavItems = listOf(
    BottomNavItem("Inicio",   Icons.Default.Home,      Dashboard, Dashboard::class),
    BottomNavItem("Horario",  Icons.Default.DateRange,  Horario,   Horario::class),
    BottomNavItem("Escáner",  Icons.Default.CameraAlt,  Scanner,   Scanner::class),
    BottomNavItem("QR",       Icons.Default.QrCode,     QR,        QR::class),
    BottomNavItem("Perfil",   Icons.Default.Person,     Perfil,    Perfil::class)
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavItems.any {
        currentDestination?.hasRoute(it.routeClass) == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hasRoute(item.routeClass) == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo<Dashboard> { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Dashboard,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable<Dashboard> {
                DashboardScreen(navController = navController)
            }
            composable<Horario> {
                HorarioScreen(navController = navController)
            }
            composable<Scanner> {
                ScannerScreen(navController = navController)
            }
            composable<QR> {
                QRScreen(navController = navController)
            }
            composable<Perfil> {
                PerfilScreen(navController = navController)
            }
            composable<Apuntes> { backStackEntry ->
                val route: Apuntes = backStackEntry.toRoute()
                ApuntesScreen(materiaId = route.materiaId, navController = navController)
            }
            composable<DetalleApunte> { backStackEntry ->
                val route: DetalleApunte = backStackEntry.toRoute()
                DetalleApunteScreen(apunteId = route.apunteId, navController = navController)
            }
            composable<CrearMateria> {
                CrearMateriaScreen(navController = navController)
            }
            composable<CrearApunte> { backStackEntry ->
                val route: CrearApunte = backStackEntry.toRoute()
                CrearApunteScreen(materiaId = route.materiaId, navController = navController)
            }
        }
    }
}