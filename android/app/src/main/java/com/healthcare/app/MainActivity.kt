package com.healthcare.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.healthcare.app.data.TokenManager
import com.healthcare.app.ui.navigation.NavGraph
import com.healthcare.app.ui.navigation.Screen
import com.healthcare.app.ui.theme.HealthCareTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HealthCareTheme {
                val navController = rememberNavController()

                // Determine start destination based on stored auth state
                val startDestination = remember {
                    val token = runBlocking { tokenManager.accessToken.first() }
                    val role = runBlocking { tokenManager.userRole.first() }
                    when {
                        token == null -> Screen.Splash.route
                        role == "doctor" -> Screen.DoctorDashboard.route
                        else -> Screen.PatientDashboard.route
                    }
                }

                NavGraph(
                    navController = navController,
                    startDestination = startDestination,
                )
            }
        }
    }
}
