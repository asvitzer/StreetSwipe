package com.asvitzer.streetswipe.data.repo

interface PaymentRepo {
    suspend fun createConnectionToken(): Result<String>
    suspend fun capturePaymentIntent(id: String): Result<Boolean>
}