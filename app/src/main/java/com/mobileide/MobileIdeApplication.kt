package com.mobileide

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Mobile IDE.
 * Initializes Hilt dependency injection.
 */
@HiltAndroidApp
class MobileIdeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any global state here
    }
}
