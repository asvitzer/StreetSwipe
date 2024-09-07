package com.asvitzer.streetswipe.di

import com.asvitzer.streetswipe.data.CustomConnectionTokenProvider
import com.asvitzer.streetswipe.data.repo.StripePaymentRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TokenModules {

    @Provides
    @Singleton
    fun provideConnectionTokenProvider(repo: StripePaymentRepo, coroutineScope: CoroutineScope): CustomConnectionTokenProvider {
        return CustomConnectionTokenProvider(repo, coroutineScope)
    }
}

