package com.asvitzer.streetswipe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asvitzer.streetswipe.data.repo.StripePaymentRepo
import com.mastercard.terminalsdk.internal.e
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.Callback
import com.stripe.stripeterminal.external.callable.Cancelable
import com.stripe.stripeterminal.external.callable.PaymentIntentCallback
import com.stripe.stripeterminal.external.models.CollectConfiguration
import com.stripe.stripeterminal.external.models.PaymentIntent
import com.stripe.stripeterminal.external.models.PaymentIntentParameters
import com.stripe.stripeterminal.external.models.TerminalException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentRequestViewModel @Inject constructor(private val stripePaymentRepo: StripePaymentRepo,
    private val terminal: Terminal) : ViewModel() {

    private var collectCancelable: Cancelable? = null

    private val _paymentStatus = MutableStateFlow<String?>(null)
    val paymentStatus: StateFlow<String?> = _paymentStatus

    fun requestPayment(amount: Long) {
        val params = PaymentIntentParameters.Builder()
            .setAmount(amount)
            .setCurrency("usd")
            .build()
        terminal.createPaymentIntent(
            params,
            object : PaymentIntentCallback {
                override fun onSuccess(paymentIntent: PaymentIntent) {
                    viewModelScope.launch(Dispatchers.Main) {
                        _paymentStatus.value = "PaymentIntent created successfully"
                    }
                    collectPaymentMethod(paymentIntent)
                }

                override fun onFailure(e: TerminalException) {
                    viewModelScope.launch(Dispatchers.Main) {
                        _paymentStatus.value = "Failed to create PaymentIntent: ${e.errorMessage}"
                    }
                }
            }
        )
    }

    private fun collectPaymentMethod(paymentIntent: PaymentIntent) {
        collectCancelable = terminal.collectPaymentMethod(
            paymentIntent,
            object : PaymentIntentCallback {
                override fun onSuccess(paymentIntent: PaymentIntent) {
                    viewModelScope.launch(Dispatchers.Main) {
                        _paymentStatus.value =  "Payment collected successfully"
                    }
                    validatePaymentIntent(paymentIntent)
                }

                override fun onFailure(e: TerminalException) {
                    viewModelScope.launch(Dispatchers.Main) {
                        _paymentStatus.value = "Failed to collect payment: ${e.errorMessage}"
                    }
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
        terminal.confirmPaymentIntent(
            paymentIntent,
            object : PaymentIntentCallback {
                override fun onSuccess(paymentIntent: PaymentIntent) {
                    viewModelScope.launch(Dispatchers.Main) {
                        _paymentStatus.value =  "Payment confirmed successfully"
                    }
                    capturePayment(paymentIntent)
                }

                override fun onFailure(e: TerminalException) {
                    viewModelScope.launch(Dispatchers.Main) {
                        _paymentStatus.value = "Failed to confirm payment: ${e.errorMessage}"
                    }
                }
            }
        )
    }

    private fun capturePayment(paymentIntent: PaymentIntent) {
        paymentIntent.id?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
               val result =  stripePaymentRepo.capturePaymentIntent(id)

                if(result.isSuccess){
                    viewModelScope.launch(Dispatchers.Main) {
                        _paymentStatus.value =  "paymentIntent successfully captured."
                    }
                } else {
                    viewModelScope.launch(Dispatchers.Main) {
                        _paymentStatus.value = "paymentIntent not successfully captured."
                    }
                }
            }
        } ?: run {
            viewModelScope.launch(Dispatchers.Main) {
                _paymentStatus.value = "paymentIntent ID is null, cannot capture payment"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        collectCancelable?.cancel(object : Callback {
            override fun onSuccess() {
                // Handle cancellation success
            }

            override fun onFailure(e: TerminalException) {
                // Handle cancellation failure
            }
        })
    }
}