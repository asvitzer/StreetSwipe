package com.asvitzer.streetswipe.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.asvitzer.streetswipe.ui.screen.PaymentOverviewScreen
import com.asvitzer.streetswipe.ui.screen.PaymentRequestScreen

@Composable
fun StreetSwipeNavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Request.route) {
        composable(Request.route) {
            PaymentRequestScreen(navController)
        }
        composable(Overview.route) {
            PaymentOverviewScreen(navController)
        }
    }
}