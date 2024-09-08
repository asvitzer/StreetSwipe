package com.asvitzer.streetswipe.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.asvitzer.streetswipe.nav.Retry
import com.asvitzer.streetswipe.ui.viewmodel.MainViewModel

@Composable
fun RetryScreen(navController: NavHostController,
                viewModel: MainViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Failed to connect to reader")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            viewModel.initialize()
            navController.navigate("loading") {
                popUpTo(Retry.route) { inclusive = true }
            }
        }) {
            Text("Retry")
        }
    }
}