package com.asvitzer.streetswipe.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asvitzer.streetswipe.R
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
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val application: Application // Inject Application context to access resources
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
                postToastMessage(application.getString(R.string.reader_disconnected))
            }
        }

        terminal.setTerminalListener(listener)

        viewModelScope.launch {
            val result = withContext(ioDispatcher) {
                stripePaymentRepo.createConnectionToken()
            }

            result.onSuccess { token ->
                postToastMessage(application.getString(R.string.connection_token_success, token))
                discoverReaders()
            }.onFailure { exception ->
                postErrorMessage(application.getString(R.string.connection_token_failed, exception.message))
                _isLoading.value = false
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun discoverReaders() {
        val config = DiscoveryConfiguration.LocalMobileDiscoveryConfiguration(isSimulated = true)

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
                    postToastMessage(application.getString(R.string.reader_discovered))
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
            val config = ConnectionConfiguration.LocalMobileConnectionConfiguration("{{LOCATION_ID}}")
            terminal.connectLocalMobileReader(reader, config, object : ReaderCallback {
                override fun onSuccess(reader: Reader) {
                    postToastMessage(application.getString(R.string.reader_connected_success))
                    hasReader = true
                    _isLoading.value = false
                }

                override fun onFailure(e: TerminalException) {
                    postErrorMessage(application.getString(R.string.reader_connection_failed, e.message))
                    _isLoading.value = false
                }
            })
        }
    }

    private fun handleTerminalError(e: TerminalException): String {
        return when (e.errorCode) {
            TerminalException.TerminalErrorCode.STRIPE_API_CONNECTION_ERROR ->
                application.getString(R.string.internet_connection_failed)
            TerminalException.TerminalErrorCode.LOCAL_MOBILE_UNSUPPORTED_ANDROID_VERSION ->
                application.getString(R.string.upgrade_os)
            else -> application.getString(R.string.reader_discovery_failed)
        }
    }

    override fun onCleared() {
        super.onCleared()
        discoverCancelable?.cancel(object : Callback {
            override fun onFailure(e: TerminalException) {
                postErrorMessage(application.getString(R.string.cancel_discovery_failed, e.errorMessage))
            }

            override fun onSuccess() {
                postToastMessage(application.getString(R.string.cancel_discovery_success))
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