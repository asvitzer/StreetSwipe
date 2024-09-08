package com.asvitzer.streetswipe

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.asvitzer.streetswipe.nav.StreetSwipeNavGraph
import com.asvitzer.streetswipe.ui.theme.StreetSwipeTheme
import com.asvitzer.streetswipe.ui.viewmodel.MainViewModel
import com.stripe.stripeterminal.external.callable.ReaderListener
import com.stripe.stripeterminal.external.models.ReaderDisplayMessage
import com.stripe.stripeterminal.external.models.ReaderInputOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity(), ReaderListener {

    private val viewModel: MainViewModel by viewModels()

    // Declare an ActivityResultLauncher for requesting location permission
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerPermissionLauncher()
        handlePermissionsAndInitialize()

        enableEdgeToEdge()
        setContent {
            StreetSwipeTheme {
                StreetSwipeNavGraph()
            }
        }

        observeViewModel()
    }

    private fun registerPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, proceed with initializing the terminal
                viewModel.initialize()
            } else {
                // Permission denied, show an error message and possibly exit
                showPermissionDeniedMessage()
            }
        }
    }

    private fun handlePermissionsAndInitialize() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted, proceed with terminal initialization
                viewModel.initialize()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                ACCESS_FINE_LOCATION
            ) -> {
                // Show rationale dialog to explain why the app needs the permission
                showPermissionRationaleDialog()
            }

            else -> {
                // Directly request permission if it hasn't been requested before
                requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Location Permission Required")
            .setMessage("This app needs access to your location in order to connect to nearby Stripe card readers.")
            .setPositiveButton("OK") { _, _ ->
                // Request permission after showing rationale
                requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                showPermissionDeniedMessage()
            }
            .show()
    }

    private fun observeViewModel() {
        viewModel.toastMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        viewModel.errorMessage.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPermissionDeniedMessage() {
        Toast.makeText(this, "Location services are required to connect to a reader. Please enable in app settings and restart app", Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onRequestReaderInput(options: ReaderInputOptions) {
        super.onRequestReaderInput(options)
        Toast.makeText(baseContext, options.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun onRequestReaderDisplayMessage(message: ReaderDisplayMessage) {
        super.onRequestReaderDisplayMessage(message)
        Toast.makeText(baseContext, message.toString(), Toast.LENGTH_SHORT).show()
    }
}

