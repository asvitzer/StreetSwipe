package com.asvitzer.streetswipe.di

import android.content.Context
import com.asvitzer.streetswipe.data.CustomConnectionTokenProvider
import com.asvitzer.streetswipe.data.repo.StripePaymentRepo
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.TerminalListener
import com.stripe.stripeterminal.external.models.Reader
import com.stripe.stripeterminal.log.LogLevel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    fun provideTerminal(
        @ApplicationContext context: Context,
        tokenProvider: CustomConnectionTokenProvider
    ): Terminal {
        val listener = object : TerminalListener {
            override fun onUnexpectedReaderDisconnect(reader: Reader) {
                // Can be blank. Listener added later but needed for init
            }
        }

        // Initialize the Terminal if it's not initialized
        if (!Terminal.isInitialized()) {
            Terminal.initTerminal(context, LogLevel.VERBOSE, tokenProvider, listener)
        }

        return Terminal.getInstance()
    }
}

