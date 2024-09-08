package com.asvitzer.streetswipe.ui.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asvitzer.streetswipe.data.repo.StripePaymentRepo
import com.asvitzer.streetswipe.di.IoDispatcher
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.Callback
import com.stripe.stripeterminal.external.callable.Cancelable
import com.stripe.stripeterminal.external.callable.DiscoveryListener
import com.stripe.stripeterminal.external.callable.ReaderCallback
import com.stripe.stripeterminal.external.callable.TerminalListener
import com.stripe.stripeterminal.external.models.ConnectionConfiguration
import com.stripe.stripeterminal.external.models.DiscoveryConfiguration
import com.stripe.stripeterminal.external.models.Reader
import com.stripe.stripeterminal.external.models.TerminalException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val stripePaymentRepo: StripePaymentRepo,
    private val terminal: Terminal,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var discoverCancelable: Cancelable? = null

    // Track whether the terminal is initialized with a reader
    var hasReader = false

    fun initialize() {
        if (hasReader) {
            return
        }

        _isLoading.value = true
        val listener = object : TerminalListener {
            override fun onUnexpectedReaderDisconnect(reader: Reader) {
                postToastMessage("Reader disconnected. Launch app again to reconnect")
            }
        }

        terminal.setTerminalListener(listener)

        viewModelScope.launch {
            // Call the createConnectionToken() and get the Result<String>
            val result = withContext(ioDispatcher) {
                stripePaymentRepo.createConnectionToken()
            }

            result.onSuccess { token ->
                postToastMessage("Successful! Token: $token")

                // Proceed to discover readers
                discoverReaders()
            }.onFailure { exception ->
                postErrorMessage("Failed to get connection token: ${exception.message}")
                _isLoading.value = false
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun discoverReaders() {
        val config =
            DiscoveryConfiguration.LocalMobileDiscoveryConfiguration(isSimulated = true)

        discoverCancelable = terminal.discoverReaders(
            config,
            object : DiscoveryListener {

                override fun onUpdateDiscoveredReaders(readers: List<Reader>) {
                    if (readers.isNotEmpty()) {
                        connectToReader(readers.first())
                    }
                }
            },
            object : Callback {
                override fun onSuccess() {
                    postToastMessage("Reader discovered successfully")
                }

                override fun onFailure(e: TerminalException) {
                    postErrorMessage(handleTerminalError(e))
                    _isLoading.value = false
                }
            }
        )
    }

    private fun connectToReader(reader: Reader) {
        viewModelScope.launch {
            val config =
                ConnectionConfiguration.LocalMobileConnectionConfiguration("{{LOCATION_ID}}")
            terminal.connectLocalMobileReader(reader, config, object :
                ReaderCallback {
                override fun onSuccess(reader: Reader) {
                    postToastMessage("Connected to reader successfully")
                    hasReader = true
                    _isLoading.value = false
                }

                override fun onFailure(e: TerminalException) {
                    postErrorMessage("Failed to connect to reader: ${e.message}")
                    _isLoading.value = false
                }
            })
        }
    }

    private fun handleTerminalError(e: TerminalException): String {
        return when (e.errorCode) {
            TerminalException.TerminalErrorCode.STRIPE_API_CONNECTION_ERROR ->
                "Cannot connect to the internet. Please try again later."

            TerminalException.TerminalErrorCode.LOCAL_MOBILE_UNSUPPORTED_ANDROID_VERSION ->
                "Please upgrade OS and try again."

            else -> "Failed to find compatible reader."
        }
    }

    override fun onCleared() {
        super.onCleared()
        discoverCancelable?.cancel(callback = object : Callback {
            override fun onFailure(e: TerminalException) {
                postErrorMessage("Could not cancel discoverCancelable: ${e.errorMessage}")
            }

            override fun onSuccess() {
                postToastMessage("Canceled discoverCancelable successfully")
            }
        })
    }

    // Helper method to emit toast messages
    private fun postToastMessage(message: String) {
        viewModelScope.launch {
            _toastMessage.emit(message)
        }
    }

    // Helper method to emit error messages
    private fun postErrorMessage(error: String) {
        viewModelScope.launch {
            _errorMessage.emit(error)
        }
    }
}