package com.asvitzer.streetswipe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asvitzer.streetswipe.data.repo.StripePaymentRepo
import com.mastercard.terminalsdk.internal.e
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.PaymentIntentCallback
import com.stripe.stripeterminal.external.models.CollectConfiguration
import com.stripe.stripeterminal.external.models.PaymentIntent
import com.stripe.stripeterminal.external.models.PaymentIntentParameters
import com.stripe.stripeterminal.external.models.TerminalException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentRequestViewModel @Inject constructor(private val stripePaymentRepo: StripePaymentRepo) : ViewModel() {

    fun requestPayment(amount: Long) {

        val params = PaymentIntentParameters.Builder()
            .setAmount(amount)
            .setCurrency("usd")
            .build()
        Terminal.getInstance().createPaymentIntent(
            params,
            object : PaymentIntentCallback {
                override fun onSuccess(paymentIntent: PaymentIntent) {
                    println("PaymentIntent created successfully: $paymentIntent")

                    collectPaymentMethod(paymentIntent)
                }

                override fun onFailure(e: TerminalException) {
                    viewModelScope.launch {
                        println("PaymentIntent failed to create successfully: ${e.errorMessage}")
                    }
                }
            }
        )
    }

    private fun collectPaymentMethod(paymentIntent: PaymentIntent) {
        //TODO cancel cancelable with viewmodel lifecycle
        val cancelable = Terminal.getInstance().collectPaymentMethod(
            paymentIntent,
            object : PaymentIntentCallback {
                override fun onSuccess(paymentIntent: PaymentIntent) {
                    println("payment successfully collected: $paymentIntent")
                    validatePaymentIntent(paymentIntent)
                }

                override fun onFailure(e: TerminalException) {
                    // Placeholder for handling exception
                    println("payment not successfully collected: ${e.errorMessage}")
                }
            }
        )
    }

    private fun validatePaymentIntent(paymentIntent: PaymentIntent) {
        val pm = paymentIntent.paymentMethod
        val card = pm?.cardPresentDetails ?: pm?.interacPresentDetails

        card?.last4?.let{
            println("Card last 4: " + card.last4)
        }
        confirmPaymentIntent(paymentIntent)
    }

    private fun confirmPaymentIntent(paymentIntent: PaymentIntent) {
        Terminal.getInstance().confirmPaymentIntent(
            paymentIntent,
            object : PaymentIntentCallback {
                override fun onSuccess(paymentIntent: PaymentIntent) {
                    println("payment successfully confirmed: $paymentIntent")

                    capturePayment(paymentIntent)
                }

                override fun onFailure(e: TerminalException) {
                    println("payment not successfully confirmed: ${e.errorMessage}")
                }
            }
        )
    }

    private fun capturePayment(paymentIntent: PaymentIntent) {
        paymentIntent.id?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
               val result =  stripePaymentRepo.capturePaymentIntent(id)

                if(result.isSuccess){
                    println("paymentIntent successfully captured.")
                } else {
                    println("paymentIntent not successfully captured.")
                }
            }
        } ?: println("paymentIntent.id is null. Payment not captured.")
    }
}