package com.asvitzer.streetswipe

import android.app.Application
import com.stripe.stripeterminal.TerminalApplicationDelegate
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StreetSwipeApp: Application() {
    override fun onCreate() {
        super.onCreate()
        // Used to inform the SDK of lifecycle events
        TerminalApplicationDelegate.onCreate(this)
    }
}