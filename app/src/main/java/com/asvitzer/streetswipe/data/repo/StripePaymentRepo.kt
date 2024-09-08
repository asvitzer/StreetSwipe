package com.asvitzer.streetswipe.data.repo

import com.asvitzer.streetswipe.data.source.PaymentApi
import com.stripe.stripeterminal.external.models.ConnectionTokenException
import java.io.IOException
import javax.inject.Inject

class StripePaymentRepo @Inject constructor(private val service: PaymentApi): PaymentRepo {

    override suspend fun createConnectionToken(): Result<String> {
        return try {
            val response = service.getConnectionToken()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.secret)
            } else {
                Result.failure(ConnectionTokenException("Creating connection token failed"))
            }
        } catch (e: IOException) {
            Result.failure(ConnectionTokenException("Creating connection token failed", e))
        }
    }

    // Refactored to return Result<Boolean>
    override suspend fun capturePaymentIntent(id: String): Result<Boolean> {
        return try {
            val response = service.capturePaymentIntent(id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to capture payment intent"))
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
}