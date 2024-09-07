package com.asvitzer.streetswipe.data.source

import com.asvitzer.streetswipe.data.model.ConnectionToken
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * The `BackendService` interface handles the two simple calls we need to make to our backend.
 */
interface PaymentApi {

    /**
     * Get a connection token string from the backend
     */
    @POST("connection_token")
    suspend fun getConnectionToken(): Call<ConnectionToken>

    /**
     * Capture a specific payment intent on our backend
     */
    @FormUrlEncoded
    @POST("capture_payment_intent")
    suspend fun capturePaymentIntent(@Field("payment_intent_id") id: String): Call<Void>

    /**
     * Cancel a specific payment intent on our backend
     */
    @FormUrlEncoded
    @POST("cancel_payment_intent")
    suspend fun cancelPaymentIntent(@Field("payment_intent_id") id: String): Call<Void>
}
