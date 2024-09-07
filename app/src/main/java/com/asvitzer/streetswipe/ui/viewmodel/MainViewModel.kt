package com.asvitzer.streetswipe.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asvitzer.streetswipe.data.CustomConnectionTokenProvider
import com.asvitzer.streetswipe.data.repo.StripePaymentRepo
import com.asvitzer.streetswipe.di.IoDispatcher
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.Callback
import com.stripe.stripeterminal.external.callable.Cancelable
import com.stripe.stripeterminal.external.callable.DiscoveryListener
import com.stripe.stripeterminal.external.callable.ReaderCallback
import com.stripe.stripeterminal.external.callable.TerminalListener
import com.stripe.stripeterminal.external.models.ConnectionConfiguration
import com.stripe.stripeterminal.external.models.ConnectionTokenException
import com.stripe.stripeterminal.external.models.DiscoveryConfiguration
import com.stripe.stripeterminal.external.models.Reader
import com.stripe.stripeterminal.external.models.TerminalException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val stripePaymentRepo: StripePaymentRepo,
    private val terminal: Terminal,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private var discoverCancelable: Cancelable? = null

    init {
        initialize()
    }

    private fun initialize() {
        val listener = object : TerminalListener {
            override fun onUnexpectedReaderDisconnect(reader: Reader) {
                _toastMessage.postValue("Reader disconnected. Launch app again to reconnect")
            }
        }

        terminal.setTerminalListener(listener)

        viewModelScope.launch {
            try {
                val token = withContext(ioDispatcher) {
                    stripePaymentRepo.createConnectionToken()
                }

                _toastMessage.postValue("Successful! Token: $token")

                discoverReaders()

            } catch (exception: ConnectionTokenException) {
                _errorMessage.postValue("Failed to get connection token: ${exception.message}")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun discoverReaders() {
        val isDebuggable = 0 != (ApplicationInfo.FLAG_DEBUGGABLE)
        val config =
            DiscoveryConfiguration.LocalMobileDiscoveryConfiguration(isSimulated = isDebuggable)

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
                    _toastMessage.postValue("Reader discovered successfully")
                }

                override fun onFailure(e: TerminalException) {
                    _errorMessage.postValue(handleTerminalError(e))
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
                    _toastMessage.postValue("Connected to reader successfully")
                }

                override fun onFailure(e: TerminalException) {
                    _errorMessage.postValue("Failed to connect to reader: ${e.message}")
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
                _errorMessage.postValue("Could not cancel discoverCancelable: ${e.errorMessage}")
            }

            override fun onSuccess() {
                _toastMessage.postValue("Canceled discoverCancelable successfully")
            }
        })
    }
}