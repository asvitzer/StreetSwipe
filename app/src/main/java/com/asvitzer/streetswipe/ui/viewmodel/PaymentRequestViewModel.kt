package com.asvitzer.streetswipe.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asvitzer.streetswipe.R
import com.asvitzer.streetswipe.data.repo.StripePaymentRepo
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.Callback
import com.stripe.stripeterminal.external.callable.Cancelable
import com.stripe.stripeterminal.external.callable.PaymentIntentCallback
import com.stripe.stripeterminal.external.models.PaymentIntent
import com.stripe.stripeterminal.external.models.PaymentIntentParameters
import com.stripe.stripeterminal.external.models.TerminalException
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PaymentRequestViewModel @Inject constructor(
    application: Application,
    private val stripePaymentRepo: StripePaymentRepo,
    private val terminal: Terminal
) : AndroidViewModel(application) {

    private var collectCancelable: Cancelable? = null

    private val _paymentStatus = MutableStateFlow<String?>(null)
    val paymentStatus: StateFlow<String?> = _paymentStatus

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun requestPayment(amount: Long) {
        _isLoading.value = true
        val params = PaymentIntentParameters.Builder()
            .setAmount(amount)
            .setCurrency("usd")
            .build()
        terminal.createPaymentIntent(
            params,
            object : PaymentIntentCallback {
                override fun onSuccess(paymentIntent: PaymentIntent) {
                    viewModelScope.launch(Dispatchers.Main) {
                        _paymentStatus.value = getApplication<Application>().getString(R.string.payment_intent_created_success)
                    }
                    collectPaymentMethod(paymentIntent)
                }

                override fun onFailure(e: TerminalException) {
                    viewModelScope.launch(Dispatchers.Main) {
                        _paymentStatus.value = getApplication<Application>().getString(R.string.payment_intent_creation_failed, e.errorMessage)
                        _isLoading.value = false
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
                        _paymentStatus.value = getApplication<Application>().getString(R.string.payment_collected_success)
                    }
                    validatePaymentIntent(paymentIntent)
                }

                override fun onFailure(e: TerminalException) {
                    viewModelScope.launch(Dispatchers.Main) {
                        _paymentStatus.value = getApplication<Application>().getString(R.string.payment_collection_failed, e.errorMessage)
                        _isLoading.value = false
                    }
                }
            }
        )
    }

    private fun validatePaymentIntent(paymentIntent: PaymentIntent) {
        val pm = paymentIntent.paymentMethod
        val card = pm?.cardPresentDetails ?: pm?.interacPresentDetails

        card?.last4?.let {
            println("Card last 4: " + it)
        }
        confirmPaymentIntent(paymentIntent)
    }

    private fun confirmPaymentIntent(paymentIntent: PaymentIntent) {
        terminal.confirmPaymentIntent(
            paymentIntent,
            object : PaymentIntentCallback {
                override fun onSuccess(paymentIntent: PaymentIntent) {
                    viewModelScope.launch(Dispatchers.Main) {
                        _paymentStatus.value = getApplication<Application>().getString(R.string.payment_confirmed_success)
                    }
                    capturePayment(paymentIntent)
                }

                override fun onFailure(e: TerminalException) {
                    viewModelScope.launch(Dispatchers.Main) {
                        _paymentStatus.value = getApplication<Application>().getString(R.string.payment_confirmation_failed, e.errorMessage)
                        _isLoading.value = false
                    }
                }
            }
        )
    }

    private fun capturePayment(paymentIntent: PaymentIntent) {
        paymentIntent.id?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
                val result = stripePaymentRepo.capturePaymentIntent(id)

                withContext(Dispatchers.Main) {
                    result.onSuccess {
                        _paymentStatus.value = getApplication<Application>().getString(R.string.payment_capture_success)
                        _isLoading.value = false
                    }.onFailure { exception ->
                        _paymentStatus.value = getApplication<Application>().getString(R.string.payment_capture_failed, exception.message)
                        _isLoading.value = false
                    }
                }
            }
        } ?: run {
            _paymentStatus.value = getApplication<Application>().getString(R.string.payment_intent_id_null)
            _isLoading.value = false
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

    fun clearPaymentStatus() {
        _paymentStatus.value = null
    }
}