package com.contracts.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.contracts.app.domain.ContractViewModel
import com.contracts.app.notifications.DeadlineWorker
import com.contracts.app.notifications.createChannel
import com.contracts.app.ui.screens.*
import com.contracts.app.ui.theme.ContractsTheme
import com.contracts.app.ui.theme.appColors

class MainActivity : ComponentActivity() {

    private val vm: ContractViewModel by viewModels()

    private val notifLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* silent */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createChannel(this)
        requestNotifPermission()
        DeadlineWorker.schedule(this)

        setContent {
            val state by vm.state.collectAsState()
            ContractsTheme(state.themeId, state.customColors) {
                AppNavigation(vm)
            }
        }
    }

    private fun requestNotifPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
fun AppNavigation(vm: ContractViewModel) {
    val navController = rememberNavController()
    val c = appColors()

    val bottomItems = listOf(
        Triple("dashboard", Icons.Default.Dashboard,     "داشبورد"),
        Triple("list",      Icons.Default.List,          "قراردادها"),
        Triple("calendar",  Icons.Default.CalendarMonth, "تقویم"),
        Triple("settings",  Icons.Default.Settings,      "تنظیمات"),
    )

    val navBack by navController.currentBackStackEntryAsState()
    val currentRoute = navBack?.destination?.route

    Scaffold(
        containerColor = c.background,
        bottomBar = {
            if (currentRoute?.startsWith("detail") != true) {
                NavigationBar(containerColor = c.surfaceElevated, tonalElevation = 0.dp) {
                    bottomItems.forEach { (route, icon, label) ->
                        val selected = navBack?.destination?.hierarchy
                            ?.any { it.route == route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick  = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon   = { Icon(icon, label) },
                            label  = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = c.primary,
                                selectedTextColor   = c.primary,
                                unselectedIconColor = c.textMuted,
                                unselectedTextColor = c.textMuted,
                                indicatorColor      = c.primary.copy(0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            NavHost(navController = navController, startDestination = "dashboard") {

                composable("dashboard") {
                    DashboardScreen(
                        vm           = vm,
                        onGoToList   = { navController.navigate("list") },
                        onContractClick = { id -> navController.navigate("detail/$id") }
                    )
                }

                composable("list") {
                    ContractListScreen(
                        vm              = vm,
                        onContractClick = { id -> navController.navigate("detail/$id") }
                    )
                }

                composable("calendar") {
                    CalendarScreen(
                        vm              = vm,
                        onContractClick = { id -> navController.navigate("detail/$id") }
                    )
                }

                composable("settings") {
                    SettingsScreen(vm = vm)
                }

                composable("detail/{id}") { back ->
                    val id = back.arguments?.getString("id")?.toIntOrNull()
                        ?: return@composable
                    ContractDetailScreen(
                        id     = id,
                        vm     = vm,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
