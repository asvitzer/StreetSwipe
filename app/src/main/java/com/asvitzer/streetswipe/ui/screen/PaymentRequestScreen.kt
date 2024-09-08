package com.asvitzer.streetswipe.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.asvitzer.streetswipe.ui.viewmodel.PaymentRequestViewModel

@Composable
fun PaymentRequestScreen(
    viewModel: PaymentRequestViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val paymentStatus by viewModel.paymentStatus.collectAsState()

    PaymentRequestComponent(
        onSubmitAmount = { amount -> viewModel.requestPayment(amount) }
    )

    LaunchedEffect(paymentStatus) {
        paymentStatus?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
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
                // Filter the new value as the user types to only allow digits
                val filteredValue = newValue.filter { it.isDigit() }
                amount = filteredValue
            },
            label = { Text("Enter Amount") },
            modifier = Modifier.padding(16.dp)
        )

        Button(
            onClick = {
                val long = amount.toLongOrNull()
                onSubmitAmount(long ?: 0) },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Pay")
        }
    }
}

@Preview
@Composable
fun PaymentRequestScreenPreview() {
    PaymentRequestComponent { }
}