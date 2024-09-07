package com.asvitzer.streetswipe.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.PaymentIntentCallback
import com.stripe.stripeterminal.external.models.PaymentIntent
import com.stripe.stripeterminal.external.models.PaymentIntentParameters
import com.stripe.stripeterminal.external.models.TerminalException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PaymentRequestViewModel @Inject constructor(
) : ViewModel() {
    fun requestPayment(amount: Long){
        val params = PaymentIntentParameters.Builder()
            .setAmount(amount)
            .setCurrency("usd")
            .build()
        Terminal.getInstance().createPaymentIntent(
            params,
            object : PaymentIntentCallback {
                override fun onSuccess(paymentIntent: PaymentIntent) {
                    // Placeholder for handling successful operation
                    if(true){
                        println("We true")
                    }
                }

                override fun onFailure(e: TerminalException) {
                    // Placeholder for handling exception
                    if(true){
                        println("We true")
                    }
                }
            }
        )
    }
}