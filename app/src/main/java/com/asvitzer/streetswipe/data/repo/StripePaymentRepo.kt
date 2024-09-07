package com.asvitzer.streetswipe.data.repo

import com.asvitzer.streetswipe.data.source.PaymentApi
import com.stripe.stripeterminal.external.models.ConnectionTokenException
import java.io.IOException
import javax.inject.Inject

class StripePaymentRepo @Inject constructor(private val service: PaymentApi): PaymentRepo {

    @Throws(ConnectionTokenException::class)
    override suspend fun createConnectionToken(): String {
        try {
            val result = service.getConnectionToken()
            if (result.isSuccessful && result.body() != null) {
                return result.body()!!.secret
            } else {
                throw ConnectionTokenException("Creating connection token failed")
            }
        } catch (e: IOException) {
            throw ConnectionTokenException("Creating connection token failed", e)
        }
    }

    override suspend fun capturePaymentIntent(id: String): Result<Boolean> {
        val result = service.capturePaymentIntent(id)
        return Result.success(result.isSuccessful)
    }
}