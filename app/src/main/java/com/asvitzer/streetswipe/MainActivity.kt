package com.asvitzer.streetswipe

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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.asvitzer.streetswipe.data.repo.StripePaymentRepo
import com.asvitzer.streetswipe.di.IoDispatcher
import com.asvitzer.streetswipe.nav.Overview
import com.asvitzer.streetswipe.nav.Request
import com.asvitzer.streetswipe.nav.StreetSwipeNavGraph
import com.asvitzer.streetswipe.ui.screen.PaymentOverviewScreen
import com.asvitzer.streetswipe.ui.screen.PaymentRequestScreen
import com.asvitzer.streetswipe.ui.theme.StreetSwipeTheme
import com.stripe.stripeterminal.external.models.ConnectionTokenException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var stripePaymentRepo: StripePaymentRepo

    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
        enableEdgeToEdge()
        setContent {
            StreetSwipeNavGraph()
        }
    }

    //TODO Move to UseCase/ViewModel
    private fun initialize() {
        lifecycleScope.launch {
            try {
                val token = withContext(ioDispatcher) {
                    stripePaymentRepo.createConnectionToken()
                }

                Toast.makeText(this@MainActivity, "Successful! Token: $token", Toast.LENGTH_SHORT)
                    .show()

            } catch (exception: ConnectionTokenException) {
                Toast.makeText(baseContext, "Failed to get token", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

