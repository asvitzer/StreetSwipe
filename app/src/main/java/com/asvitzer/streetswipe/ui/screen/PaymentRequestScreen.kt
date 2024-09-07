package com.asvitzer.streetswipe.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.asvitzer.streetswipe.ui.viewmodel.PaymentRequestViewModel

@Composable
fun PaymentRequestScreen(
    navController: NavHostController,
    viewModel: PaymentRequestViewModel = hiltViewModel()
) {

    PaymentRequestComponent(onSubmitAmount = { amount ->
        viewModel.requestPayment(amount)
    })
}

@Composable
private fun PaymentRequestComponent(onSubmitAmount: (Long) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        var amount by rememberSaveable { mutableStateOf("") }

        TextField(
            value = amount,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
                    amount = newValue
                }
            },
            label = { Text("Enter Amount") },
            modifier = Modifier.padding(16.dp)
        )

        Button(
            onClick = { onSubmitAmount(amount.toLongOrNull() ?: 0) },
            modifier = Modifier.padding(16.dp)
        ) {
        }
    }
}

@Preview
@Composable
fun PaymentRequestScreenPreview() {
    PaymentRequestComponent({ })
}