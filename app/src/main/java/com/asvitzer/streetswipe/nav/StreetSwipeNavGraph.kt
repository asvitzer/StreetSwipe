package com.asvitzer.streetswipe.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.asvitzer.streetswipe.ui.screen.LoadingScreen
import com.asvitzer.streetswipe.ui.screen.PaymentRequestScreen
import com.asvitzer.streetswipe.ui.screen.RetryScreen
import com.asvitzer.streetswipe.ui.viewmodel.MainViewModel

@Composable
fun StreetSwipeNavGraph(viewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Loading.route) {
        composable(Loading.route) {
            LoadingScreen()
            LaunchedEffect(Unit) {
                viewModel.isLoading.collect { isLoading ->
                    if (!isLoading) {
                        if (viewModel.hasReader) {
                            navController.navigate(Request.route)
                        } else {
                            navController.navigate(Retry.route)
                        }
                    }
                }
            }
        }
        composable("retry") {
            RetryScreen(
                onRetry = {
                    viewModel.initialize()
                    navController.navigate(Loading.route)
                }
            )
        }
        composable(Request.route) {
            PaymentRequestScreen()
        }
    }
}