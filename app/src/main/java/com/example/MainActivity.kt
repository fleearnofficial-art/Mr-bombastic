package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.FleearnRepository
import com.example.ui.admin.AdminMainScreen
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.SignUpScreen
import com.example.ui.auth.WelcomeScreen
import com.example.ui.components.ExitConfirmationDialog
import com.example.ui.components.InAppBrowserContainer
import com.example.ui.components.LanguageManager
import com.example.ui.components.OfflineScreen
import com.example.ui.moderator.ModeratorMainScreen
import com.example.ui.publisher.PublisherMainScreen
import com.example.ui.theme.LocalThemeState
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ThemeState
import com.example.ui.user.UserMainScreen
import com.example.util.rememberNetworkConnectivityState

enum class AuthNavigationState {
    WELCOME,
    LOGIN,
    SIGN_UP,
    MAIN_APP
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeState = remember { ThemeState(initialDark = true) }
            val languageManager = remember { LanguageManager() }
            val currentUser by FleearnRepository.currentUser.collectAsState()
            val isOnline by rememberNetworkConnectivityState()
            var navState by remember {
                mutableStateOf(
                    if (currentUser != null) AuthNavigationState.MAIN_APP else AuthNavigationState.WELCOME
                )
            }
            
            val context = LocalContext.current
            var showExitDialog by remember { mutableStateOf(false) }
            var bypassOffline by remember { mutableStateOf(false) }

            // Reset bypass state when they go back online
            LaunchedEffect(isOnline) {
                if (isOnline) {
                    bypassOffline = false
                }
            }

            // Only show offline screen if they are not logged in and have not chosen to bypass.
            val shouldShowOfflineScreen = !isOnline && navState != AuthNavigationState.MAIN_APP && !bypassOffline

            LaunchedEffect(currentUser) {
                if (currentUser != null && navState != AuthNavigationState.MAIN_APP) {
                    navState = AuthNavigationState.MAIN_APP
                } else if (currentUser == null && navState == AuthNavigationState.MAIN_APP) {
                    navState = AuthNavigationState.WELCOME
                }
            }

            CompositionLocalProvider(
                LocalThemeState provides themeState,
                LanguageManager.LocalLanguage provides languageManager
            ) {
                MyApplicationTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        
                        BackHandler(enabled = navState == AuthNavigationState.MAIN_APP || navState == AuthNavigationState.WELCOME) {
                            showExitDialog = true
                        }
                        
                        if (shouldShowOfflineScreen) {
                            OfflineScreen(
                                onTryAgain = {
                                    val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
                                    val activeNetwork = connectivityManager.activeNetwork
                                    val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                                    val connected = networkCapabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                                    if (connected) {
                                        Toast.makeText(context, "Connection restored! Welcome back.", Toast.LENGTH_SHORT).show()
                                        bypassOffline = false
                                    } else {
                                        Toast.makeText(context, "Still offline. Please check your connections.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onBypassOffline = {
                                    bypassOffline = true
                                    Toast.makeText(context, "Offline mode activated.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            when (navState) {
                                AuthNavigationState.WELCOME -> {
                                    WelcomeScreen(
                                        onNavigateToLogin = { navState = AuthNavigationState.LOGIN },
                                        onNavigateToSignUp = { navState = AuthNavigationState.SIGN_UP }
                                    )
                                }
                                AuthNavigationState.LOGIN -> {
                                    LoginScreen(
                                        onBack = { navState = AuthNavigationState.WELCOME },
                                        onLoginSuccess = { user ->
                                            navState = AuthNavigationState.MAIN_APP
                                        },
                                        onNavigateToSignUp = { navState = AuthNavigationState.SIGN_UP }
                                    )
                                }
                                AuthNavigationState.SIGN_UP -> {
                                    SignUpScreen(
                                        onBack = { navState = AuthNavigationState.WELCOME },
                                        onRegistrationSuccess = {
                                            navState = AuthNavigationState.LOGIN
                                        }
                                    )
                                }
                                AuthNavigationState.MAIN_APP -> {
                                    val user = currentUser
                                    if (user != null) {
                                        when (user.role) {
                                            UserRole.USER -> UserMainScreen(
                                                user = user,
                                                onLogout = {
                                                    FleearnRepository.logout()
                                                    navState = AuthNavigationState.WELCOME
                                                }
                                            )
                                            UserRole.PUBLISHER -> PublisherMainScreen(
                                                publisher = user,
                                                onLogout = {
                                                    FleearnRepository.logout()
                                                    navState = AuthNavigationState.WELCOME
                                                }
                                            )
                                            UserRole.MODERATOR -> ModeratorMainScreen(
                                                moderator = user,
                                                onLogout = {
                                                    FleearnRepository.logout()
                                                    navState = AuthNavigationState.WELCOME
                                                }
                                            )
                                            UserRole.ADMIN -> AdminMainScreen(
                                                admin = user,
                                                onLogout = {
                                                    FleearnRepository.logout()
                                                    navState = AuthNavigationState.WELCOME
                                                }
                                            )
                                        }
                                    } else {
                                        WelcomeScreen(
                                            onNavigateToLogin = { navState = AuthNavigationState.LOGIN },
                                            onNavigateToSignUp = { navState = AuthNavigationState.SIGN_UP }
                                        )
                                    }
                                }
                            }
                        }

                        // Exit Confirmation
                        if (showExitDialog) {
                            ExitConfirmationDialog(
                                onConfirm = { finish() },
                                onDismiss = { showExitDialog = false }
                            )
                        }

                        // Global In-App Mini Browser
                        InAppBrowserContainer()
                    }
                }
            }
        }
    }
}
