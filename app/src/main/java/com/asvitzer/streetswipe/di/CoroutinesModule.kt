package com.asvitzer.streetswipe.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {

    @Provides
    @IoDispatcher
    fun providesIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    /**
     * Provides a CoroutineScope for use across the application, with a SupervisorJob and Dispatchers.IO.
     *
     * **Use with caution**: This scope is bound to the application lifecycle (SingletonComponent),
     * which means it's effectively a global scope. Be mindful when using this for operations that
     * shouldn't persist beyond a certain lifecycle (e.g., an activity or fragment), as it won't
     * automatically cancel tasks when those components are destroyed.
     *
     * **Why SupervisorJob()?**: Unlike a regular Job, SupervisorJob allows child coroutines to fail
     * independently without cancelling the entire scope, enabling more robust error handling.
     */
    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}