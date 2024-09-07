package com.asvitzer.streetswipe.data.repo

interface PaymentRepo {
    suspend fun createConnectionToken(): String
    suspend fun capturePaymentIntent(id: String): Result<Boolean>
}