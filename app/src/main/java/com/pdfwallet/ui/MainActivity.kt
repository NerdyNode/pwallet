package com.pdfwallet.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pdfwallet.data.db.DocumentType
import com.pdfwallet.data.repository.AuthRepository
import com.pdfwallet.data.repository.SettingsRepository
import com.pdfwallet.ui.auth.LoginScreen
import com.pdfwallet.ui.debug.DebugLogsScreen
import com.pdfwallet.ui.detail.DocumentDetailScreen
import com.pdfwallet.ui.detail.TicketDetailScreen
import com.pdfwallet.ui.lock.AppLockScreen
import com.pdfwallet.ui.settings.SettingsScreen
import com.pdfwallet.ui.theme.PDFWalletTheme
import com.pdfwallet.ui.theme.ThemeMode
import com.pdfwallet.util.NotificationPermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@OptIn(ExperimentalSharedTransitionApi::class)
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var authRepository: AuthRepository
    private lateinit var notificationPermissionHelper: NotificationPermissionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        notificationPermissionHelper = NotificationPermissionHelper(this)
        notificationPermissionHelper.checkAndRequestPermission()

        setContent {
            val useDynamicColor by settingsRepository.useDynamicColorFlow.collectAsStateWithLifecycle(initialValue = true)
            val themeMode by settingsRepository.themeModeFlow.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            val isUserLoggedIn by authRepository.isUserLoggedIn.collectAsStateWithLifecycle()
            
            PDFWalletTheme(themeMode = themeMode, dynamicColor = useDynamicColor) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    SharedTransitionLayout {
                        NavHost(navController = navController, startDestination = if (isUserLoggedIn) "lock" else "login") {
                            composable("login") {
                                LoginScreen(
                                    onLoginSuccess = {
                                        navController.navigate("lock") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("lock") {
                                AppLockScreen(onUnlocked = {
                                    navController.navigate("main") {
                                        popUpTo("lock") { inclusive = true }
                                    }
                                })
                            }

                            composable("main") {
                                MainScreen(
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = this@composable,
                                    onNavigateToDocumentDetail = { doc ->
                                        if (doc.documentType in listOf(
                                                DocumentType.AIRLINE,
                                                DocumentType.TRAIN,
                                                DocumentType.BUS,
                                                DocumentType.OTHER
                                            )) {
                                            navController.navigate("ticket_detail/${doc.id}")
                                        } else {
                                            navController.navigate("document_detail/${doc.id}")
                                        }
                                    },
                                    onNavigateToRoute = { route ->
                                        navController.navigate(route)
                                    }
                                )
                            }
                            composable("settings") {
                                SettingsScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToDebug = { navController.navigate("debug_logs") }
                                )
                            }

                            composable("debug_logs") {
                                DebugLogsScreen(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable("ticket_detail/{docId}") { backStackEntry ->
                                val docId = backStackEntry.arguments?.getString("docId")?.toLongOrNull() ?: -1L
                                TicketDetailScreen(
                                    docId = docId,
                                    animatedVisibilityScope = this@composable,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable("document_detail/{docId}") { backStackEntry ->
                                val docId = backStackEntry.arguments?.getString("docId")?.toLongOrNull() ?: -1L
                                DocumentDetailScreen(
                                    docId = docId,
                                    animatedVisibilityScope = this@composable,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
