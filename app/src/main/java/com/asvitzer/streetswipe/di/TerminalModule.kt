import android.content.Context
import com.asvitzer.streetswipe.data.CustomConnectionTokenProvider
import com.asvitzer.streetswipe.data.repo.StripePaymentRepo
import com.stripe.stripeterminal.BuildConfig
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
object TerminalModule {

    @Provides
    @Singleton
    fun provideTerminal(
        @ApplicationContext context: Context,
        tokenProvider: CustomConnectionTokenProvider
    ): Terminal {
        val logLevel = LogLevel.VERBOSE
        if (!Terminal.isInitialized()) {
            Terminal.initTerminal(context, logLevel, tokenProvider, object: TerminalListener{
                override fun onUnexpectedReaderDisconnect(reader: Reader) {
                    // Listener can be empty for now
                }
            })
        }
        return Terminal.getInstance()
    }

    @Provides
    @Singleton
    fun provideConnectionTokenProvider(repo: StripePaymentRepo, coroutineScope: CoroutineScope): CustomConnectionTokenProvider {
        return CustomConnectionTokenProvider(repo, coroutineScope)
    }
}