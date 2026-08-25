package com.pdfwallet.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderCopy
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.pdfwallet.data.db.Document
import com.pdfwallet.ui.home.HomeScreen
import com.pdfwallet.ui.files.FilesScreen
import com.pdfwallet.ui.collections.CollectionsScreen
import com.pdfwallet.ui.settings.SettingsScreen
import com.pdfwallet.ui.theme.Dimens

sealed class BottomNavItem(val route: String, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Home : BottomNavItem("home_tab", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Files : BottomNavItem("files_tab", "Files", Icons.Filled.Description, Icons.Outlined.Description)
    object Collections : BottomNavItem("collections_tab", "Collections", Icons.Filled.FolderCopy, Icons.Outlined.FolderCopy)
    object Settings : BottomNavItem("settings_tab", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onNavigateToDocumentDetail: (Document) -> Unit,
    onNavigateToRoute: (String) -> Unit
) {
    val navController = rememberNavController()
    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Files,
        BottomNavItem.Collections,
        BottomNavItem.Settings
    )
    
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isExpanded = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
    val isMedium = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM
    val showNavigationRail = isExpanded || isMedium

    Row(modifier = Modifier.fillMaxSize()) {
        if (showNavigationRail) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                header = {
                    Icon(
                        imageVector = Icons.Filled.Wallet,
                        contentDescription = "App Logo",
                        modifier = Modifier.padding(vertical = 16.dp).size(Dimens.IconLarge),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navItems.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationRailItem(
                        icon = { Icon(if (isSelected) item.selectedIcon else item.unselectedIcon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }

        Scaffold(
            bottomBar = {
                if (!showNavigationRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tonalElevation = 3.dp
                    ) {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination

                        navItems.forEach { item ->
                            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                            NavigationBarItem(
                                icon = { Icon(if (isSelected) item.selectedIcon else item.unselectedIcon, contentDescription = item.title) },
                                label = { Text(item.title) },
                                selected = isSelected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = BottomNavItem.Home.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                enterTransition = { fadeIn(tween(300)) + slideInVertically { it / 8 } },
                exitTransition = { fadeOut(tween(200)) }
            ) {
                composable(BottomNavItem.Home.route) {
                    HomeScreen(
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onNavigateToWallet = { /* handled by bottom nav now */ },
                        onNavigateToDebugLogs = { /* move to settings */ },
                        onNavigateToDocumentDetail = onNavigateToDocumentDetail,
                        onNavigateToCamera = { navController.navigate("camera_scanner") },
                        onNavigateToTab = { route ->
                            val targetRoute = if (route == "backup_tab") BottomNavItem.Settings.route else route
                            navController.navigate(targetRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(BottomNavItem.Files.route) {
                    FilesScreen(
                        onNavigateToDocumentDetail = onNavigateToDocumentDetail
                    )
                }
                composable(BottomNavItem.Collections.route) {
                    CollectionsScreen(
                        onNavigateToHome = {
                            navController.navigate(BottomNavItem.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(BottomNavItem.Settings.route) {
                    SettingsScreen(
                        onNavigateToDebug = { onNavigateToRoute("debug_logs") }
                    )
                }
                composable("camera_scanner") {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    var hasPermission by remember { 
                        mutableStateOf(
                            androidx.core.content.ContextCompat.checkSelfPermission(
                                context, 
                                android.Manifest.permission.CAMERA
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) 
                    }
                    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
                    
                    val permissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        hasPermission = isGranted
                        if (!isGranted) {
                            showPermissionDeniedDialog = true
                        }
                    }
                    
                    LaunchedEffect(Unit) {
                        if (!hasPermission) {
                            permissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    }
                    
                    if (showPermissionDeniedDialog) {
                        AlertDialog(
                            onDismissRequest = { 
                                showPermissionDeniedDialog = false
                                navController.popBackStack()
                            },
                            title = { Text("Permission Required") },
                            text = { Text("Camera permission is needed to scan documents. Please enable it in app settings.") },
                            confirmButton = {
                                TextButton(onClick = { 
                                    showPermissionDeniedDialog = false
                                    navController.popBackStack()
                                    // Could add intent to open settings here
                                }) {
                                    Text("OK")
                                }
                            }
                        )
                    }
                    
                    if (hasPermission) {
                        val walletViewModel: com.pdfwallet.ui.wallet.WalletViewModel = androidx.hilt.navigation.compose.hiltViewModel()
                        com.pdfwallet.ui.capture.CameraScannerScreen(
                            onImageCaptured = { uri ->
                                walletViewModel.importManualDocument(uri)
                                navController.popBackStack()
                            },
                            onCancel = { navController.popBackStack() }
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
