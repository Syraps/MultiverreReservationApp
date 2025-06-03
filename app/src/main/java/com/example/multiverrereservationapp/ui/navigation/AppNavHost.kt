package com.example.multiverrereservationapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.multiverrereservationapp.ui.screens.AdminPanelScreen
import com.example.multiverrereservationapp.ui.screens.GameReservationScreen
import com.example.multiverrereservationapp.ui.screens.HomeScreen
import com.example.multiverrereservationapp.ui.screens.LoginScreen
import com.example.multiverrereservationapp.ui.screens.ReservationScreen
import com.example.multiverrereservationapp.viewmodel.AuthViewModel
import com.example.multiverrereservationapp.viewmodel.ReservationViewModel
import com.example.multiverrereservationapp.ui.screens.UserProfileScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val reservationViewModel: ReservationViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { navController.navigate("home") }
            )
        }
        composable("home") {
            HomeScreen(
                authViewModel = authViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNavigateToReservation = { navController.navigate("reservation") },
                onNavigateToGameReservation = { navController.navigate("gameReservation") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToAdminPanel = { navController.navigate("adminPanel") }
            )
        }
        composable("reservation") {
            ReservationScreen(
                onConfirm = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable("gameReservation") {
            GameReservationScreen(
                reservationViewModel = reservationViewModel,
                onReservationSuccess = {
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }
        composable("profile") {
            UserProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("adminPanel") {
            AdminPanelScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}