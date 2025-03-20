package com.example.wazesearcher.wazeshare

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.wazesearcher.await
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.Place.Field.ADDRESS
import com.google.android.libraries.places.api.model.Place.Field.ADDRESS_COMPONENTS
import com.google.android.libraries.places.api.model.Place.Field.LAT_LNG
import com.google.android.libraries.places.api.model.Place.Field.NAME
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class WazeShareActivity : AppCompatActivity() {
    private val viewModel by viewModels<WazeShareViewModel>()
    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        placesClient = Places.createClient(this)

        setContent {
            MaterialTheme {
                WazeShareScreen(viewModel)
            }
        }

        onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val textData = intent.getStringExtra(Intent.EXTRA_TEXT)

        if (textData.isNullOrEmpty()) {
            viewModel.error()
            return
        } else {
            viewModel.loading()
        }


        textData.toString().let { url ->
            Toast.makeText(this, url, Toast.LENGTH_LONG).show()

            lifecycleScope.launch(Dispatchers.IO) {
                val resolvedUrl = resolveRedirectUrl(url)
                val searchedDestination = decodeLocationQuery(resolvedUrl)

                withContext(Dispatchers.Main) {
                    viewModel.updateUrl(searchedDestination) }

                val placeFields = listOf(ADDRESS, NAME, LAT_LNG, ADDRESS_COMPONENTS)
                val searchRequest = SearchByTextRequest.builder(searchedDestination, placeFields)
                    .setMaxResultCount(1)
                    .build()
                val wazeUrl = buildWazeUrlFromPlace(placesClient, searchRequest)

                withContext(Dispatchers.Main) {
                    if (wazeUrl != null) {
                        val intent = Intent(Intent.ACTION_VIEW, wazeUrl.toUri())
                        startActivity(intent)
                        finish()
                    } else {
                        viewModel.error()
                    }
                }
            }
        }
    }

    private suspend fun buildWazeUrlFromPlace(placesClient: PlacesClient, searchRequest: SearchByTextRequest): String? {
        val response = placesClient.searchByText(searchRequest).await()
        val place = response.places.firstOrNull()
        return createWazeUrl(place)
    }

    private fun createWazeUrl(place: Place?): String? {
        val placeNameQuery = place?.name?.let {
            "q=${URLEncoder.encode(it, StandardCharsets.UTF_8.toString())}&"
        }
        return place?.let { "https://waze.com/ul?${placeNameQuery}ll=${it.latLng?.latitude},${it.latLng?.longitude}&navigate=yes" }
    }

    private fun resolveRedirectUrl(initialUrl: String, maxRedirects: Int = 5): String {
        var url = URL(initialUrl)
        var connection: HttpURLConnection
        var redirectCount = 0

        while (redirectCount < maxRedirects) {
            connection = url.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = false // We'll handle redirects manually
            connection.connect()

            val responseCode = connection.responseCode

            if (responseCode in 300..399) {
                val redirectLocation = connection.getHeaderField("Location")
                if(redirectLocation.isNullOrBlank()){
                    return url.toString()
                }
                url = URL(redirectLocation)
                redirectCount++
            } else {
                return url.toString() // No redirect, this is the final URL
            }
        }
        throw Exception("Too many redirects for URL: $initialUrl")
    }

    private fun decodeLocationQuery(url: String): String {
        val mapsPattern = Regex(".*/maps/place/")
        val dataPattern = Regex("/data=.*")

        return url
            .replace(mapsPattern, "")
            .replace(dataPattern, "")
            .replace('+', ' ')
    }
}