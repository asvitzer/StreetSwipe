package com.asvitzer.streetswipe.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.asvitzer.streetswipe.nav.Loading
import com.asvitzer.streetswipe.nav.Request
import com.asvitzer.streetswipe.nav.Retry
import com.asvitzer.streetswipe.ui.viewmodel.MainViewModel

@Composable
fun LoadingScreen(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    val isLoading by viewModel.isLoading.collectAsState()

    // Use LaunchedEffect to only trigger navigation when isLoading changes
    LaunchedEffect(isLoading) {
        if (!isLoading) {
            if (viewModel.hasReader) {
                navController.navigate(Request.route) {
                    popUpTo(Loading.route) { inclusive = true } // Clear loading from backstack
                }
            } else {
                navController.navigate(Retry.route) {
                    popUpTo(Loading.route) { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}