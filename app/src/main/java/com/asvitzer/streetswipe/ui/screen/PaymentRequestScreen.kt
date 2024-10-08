package com.asvitzer.streetswipe.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.asvitzer.streetswipe.R
import com.asvitzer.streetswipe.ui.viewmodel.PaymentRequestViewModel

@Composable
fun PaymentRequestScreen(
    viewModel: PaymentRequestViewModel = hiltViewModel()
) {

    val paymentStatus by viewModel.paymentStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    PaymentRequestComponent(
        onSubmitAmount = { amount -> viewModel.requestPayment(amount) }, isLoading = isLoading
    )

    val context = LocalContext.current
    LaunchedEffect(paymentStatus) {
        paymentStatus?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearPaymentStatus()
        }
    }
}

@Composable
fun PaymentRequestComponent(isLoading: Boolean, onSubmitAmount: (Long) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .imePadding(),  // Add this to shift elements up when the keyboard appears,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,

        ) {

        var amount by rememberSaveable { mutableStateOf("") }
        var isButtonClicked by rememberSaveable { mutableStateOf(false) }

        // Timer to reset the button after a short delay (debounce effect)
        LaunchedEffect(isButtonClicked) {
            if (isButtonClicked) {
                kotlinx.coroutines.delay(500L) // 500ms debounce time
                isButtonClicked = false
            }
        }

        Image(
            painter = painterResource(id = R.drawable.send_money),
            contentDescription = stringResource(id = R.string.payment_content_description),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        Text(
            text = stringResource(id = R.string.payment_amount_instructions),
            modifier = Modifier.padding(16.dp)
        )

        TextField(
            value = amount,
            onValueChange = { newValue ->
                // Filter the new value as the user types to only allow digits
                val filteredValue = newValue.filter { it.isDigit() }
                amount = filteredValue
            },
            label = { Text(stringResource(id = R.string.payment_hint)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.padding(16.dp)
        )

        val isButtonEnabled = amount.isNotEmpty() && !isLoading && !isButtonClicked

        Button(
            onClick = {
                val long = amount.toLongOrNull()
                onSubmitAmount(long ?: 0)
            },
            modifier = Modifier
                .padding(start = 72.dp, end = 72.dp, top = 16.dp, bottom = 16.dp)
                .width(200.dp)
                .height(48.dp),
            enabled = isButtonEnabled
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(stringResource(id = R.string.payment_button)) // Default button text
            }
        }
    }
}

@Preview
@Composable
fun PaymentRequestScreenPreview() {
    PaymentRequestComponent(isLoading = false) { }
}