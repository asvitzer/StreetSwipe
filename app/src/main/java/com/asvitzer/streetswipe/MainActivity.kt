package com.asvitzer.streetswipe

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.asvitzer.streetswipe.data.CustomConnectionTokenProvider
import com.asvitzer.streetswipe.data.repo.StripePaymentRepo
import com.asvitzer.streetswipe.di.IoDispatcher
import com.asvitzer.streetswipe.nav.Overview
import com.asvitzer.streetswipe.nav.Request
import com.asvitzer.streetswipe.nav.StreetSwipeNavGraph
import com.asvitzer.streetswipe.ui.screen.PaymentOverviewScreen
import com.asvitzer.streetswipe.ui.screen.PaymentRequestScreen
import com.asvitzer.streetswipe.ui.theme.StreetSwipeTheme
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.Callback
import com.stripe.stripeterminal.external.callable.Cancelable
import com.stripe.stripeterminal.external.callable.DiscoveryListener
import com.stripe.stripeterminal.external.callable.ReaderCallback
import com.stripe.stripeterminal.external.callable.ReaderListener
import com.stripe.stripeterminal.external.callable.TerminalListener
import com.stripe.stripeterminal.external.models.ConnectionConfiguration
import com.stripe.stripeterminal.external.models.ConnectionTokenException
import com.stripe.stripeterminal.external.models.DiscoveryConfiguration
import com.stripe.stripeterminal.external.models.Reader
import com.stripe.stripeterminal.external.models.ReaderDisplayMessage
import com.stripe.stripeterminal.external.models.ReaderInputOptions
import com.stripe.stripeterminal.external.models.TerminalException
import com.stripe.stripeterminal.log.LogLevel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), ReaderListener {

    @Inject
    lateinit var stripePaymentRepo: StripePaymentRepo

    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    @Inject
    lateinit var tokenProvider: CustomConnectionTokenProvider

    private var discoverCancelable: Cancelable? = null

    companion object {
        private const val REQUEST_CODE_LOCATION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
        enableEdgeToEdge()
        setContent {
            StreetSwipeTheme {
                StreetSwipeNavGraph()
            }
        }
    }

    //TODO Move to UseCase/ViewModel
    private fun initialize() {
        if (ContextCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissions = arrayOf(ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_LOCATION)
        } else {

            val listener = object : TerminalListener {
                override fun onUnexpectedReaderDisconnect(reader: Reader) {
                    //TODO prompt user if they want app to rediscover readers and auto reconnect to one
                    Toast.makeText(
                        this@MainActivity,
                        "Reader disconnected. Launch app again to reconnect",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }

            val logLevel = LogLevel.VERBOSE
            if (!Terminal.isInitialized()) {
                Terminal.initTerminal(baseContext, logLevel, tokenProvider, listener)
            }

            lifecycleScope.launch {
                try {
                    val token = withContext(ioDispatcher) {
                        stripePaymentRepo.createConnectionToken()
                    }

                    Toast.makeText(
                        this@MainActivity,
                        "Successful! Token: $token",
                        Toast.LENGTH_SHORT
                    )
                        .show()

                    discoverReaders()

                } catch (exception: ConnectionTokenException) {
                    Toast.makeText(baseContext, "Failed to get token", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun discoverReaders() {
        val isApplicationDebuggable = 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
        val config = DiscoveryConfiguration.LocalMobileDiscoveryConfiguration(
            isSimulated = isApplicationDebuggable,
        )

        discoverCancelable = Terminal.getInstance().discoverReaders(
            config,
            object : DiscoveryListener {
                override fun onUpdateDiscoveredReaders(readers: List<Reader>) {
                    connectToReader(readers.first())
                }
            },
            object : Callback {
                override fun onSuccess() {

                }

                override fun onFailure(e: TerminalException) {
                    val textToShow = when (e.errorCode) {
                        TerminalException.TerminalErrorCode.STRIPE_API_CONNECTION_ERROR -> {
                            "Cannot connect to stable internet. Please try again later."
                        }

                        TerminalException.TerminalErrorCode.LOCAL_MOBILE_UNSUPPORTED_ANDROID_VERSION -> {
                            "Please upgrade OS and try again."
                        }

                        else -> {
                            "Failed to find compatible reader."
                        }
                    }
                    Toast.makeText(baseContext, textToShow, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun connectToReader(reader: Reader) {
        lifecycleScope.launch {
            val config =
                ConnectionConfiguration.LocalMobileConnectionConfiguration("{{LOCATION_ID}}")
            //TODO Move Terminal to DI
            Terminal.getInstance().connectLocalMobileReader(
                reader,
                config,
                object : ReaderCallback {
                    override fun onSuccess(reader: Reader) {
                        Toast.makeText(
                            baseContext,
                            "Successfully connected to reader",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                    override fun onFailure(e: TerminalException) {
                        Toast.makeText(
                            baseContext,
                            "Failed to connect to reader",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }
    }

    override fun onRequestReaderInput(options: ReaderInputOptions) {
        super.onRequestReaderInput(options)
        Toast.makeText(baseContext, options.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun onRequestReaderDisplayMessage(message: ReaderDisplayMessage) {
        super.onRequestReaderDisplayMessage(message)
        Toast.makeText(baseContext, message.toString(), Toast.LENGTH_SHORT).show()
    }

    @Deprecated("Convert to using Activity Result") //TODO Convert to using Activity Result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_LOCATION && grantResults.isNotEmpty()
            && grantResults[0] != PackageManager.PERMISSION_GRANTED
        ) {
            throw RuntimeException("Location services are required in order to " + "connect to a reader.")
        }
    }

    override fun onStop() {
        super.onStop()

        // If you're leaving the activity or fragment without selecting a reader,
        // make sure you cancel the discovery process or the SDK will be stuck in
        // a discover readers phase
        discoverCancelable?.cancel(
            object : Callback {
                override fun onSuccess() {
                    // Placeholder for handling successful operation
                }

                override fun onFailure(e: TerminalException) {
                    // Placeholder for handling exception
                }
            }
        )
    }
}

