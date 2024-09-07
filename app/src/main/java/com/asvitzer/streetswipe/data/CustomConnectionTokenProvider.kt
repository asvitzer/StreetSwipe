package com.asvitzer.streetswipe.data

import com.asvitzer.streetswipe.data.repo.StripePaymentRepo
import com.stripe.stripeterminal.external.callable.ConnectionTokenCallback
import com.stripe.stripeterminal.external.callable.ConnectionTokenProvider
import com.stripe.stripeterminal.external.models.ConnectionTokenException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class CustomConnectionTokenProvider(private val repo: StripePaymentRepo,
                                    private val coroutineScope: CoroutineScope
) : ConnectionTokenProvider {
    override fun fetchConnectionToken(callback: ConnectionTokenCallback) {
        coroutineScope.launch {
            try {
                val secret = repo.createConnectionToken()
                callback.onSuccess(secret)
            } catch (e: Exception) {
                callback.onFailure(ConnectionTokenException("Failed to fetch connection token", e))
            }
        }
    }
}