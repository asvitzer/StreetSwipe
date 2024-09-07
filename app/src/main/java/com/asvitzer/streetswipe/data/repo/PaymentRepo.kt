package com.asvitzer.streetswipe.data.repo

interface PaymentRepo {
    suspend fun createConnectionToken(): String
}