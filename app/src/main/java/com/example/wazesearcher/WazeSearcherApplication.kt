package com.example.wazesearcher

import android.app.Application
import com.google.android.libraries.places.api.Places

class WazeSearcherApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val apiKey = getString(R.string.places_api_key)
        Places.initialize(applicationContext, apiKey)
    }
}