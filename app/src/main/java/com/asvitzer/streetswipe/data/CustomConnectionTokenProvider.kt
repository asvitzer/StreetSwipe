package com.asvitzer.streetswipe.data

import com.asvitzer.streetswipe.data.repo.StripePaymentRepo
import com.mastercard.terminalsdk.internal.e
import com.stripe.stripeterminal.external.callable.ConnectionTokenCallback
import com.stripe.stripeterminal.external.callable.ConnectionTokenProvider
import com.stripe.stripeterminal.external.models.ConnectionTokenException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class CustomConnectionTokenProvider(
    private val repo: StripePaymentRepo,
    private val coroutineScope: CoroutineScope
) : ConnectionTokenProvider {
    override fun fetchConnectionToken(callback: ConnectionTokenCallback) {
        coroutineScope.launch {
            val result = repo.createConnectionToken()

            if (result.isSuccess) {
                val secret = result.getOrNull()
                if (secret != null) {
                    callback.onSuccess(secret)
                } else {
                    callback.onFailure(ConnectionTokenException("Connection token is null"))
                }
            } else {
                callback.onFailure(ConnectionTokenException("Failed to fetch connection token"))
            }
        }
    }
}