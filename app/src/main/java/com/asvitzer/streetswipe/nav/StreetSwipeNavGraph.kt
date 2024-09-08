package com.asvitzer.streetswipe.nav

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.asvitzer.streetswipe.ui.screen.LoadingScreen
import com.asvitzer.streetswipe.ui.screen.PaymentRequestScreen
import com.asvitzer.streetswipe.ui.screen.RetryScreen
import com.asvitzer.streetswipe.ui.viewmodel.MainViewModel

@Composable
fun StreetSwipeNavGraph(
    navController: NavHostController = rememberNavController(),
    viewModel: MainViewModel = hiltViewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Loading.route
    ) {
        composable(Loading.route) {
            LoadingScreen(navController = navController, viewModel)
        }
        composable(Retry.route) {
            RetryScreen(navController = navController, viewModel)
        }
        composable(Request.route) {
            PaymentRequestScreen()
        }
    }
}