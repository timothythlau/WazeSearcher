package com.example.wazesearcher.wazeshare

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun WazeShareScreen(viewModel: WazeShareViewModel) {
    val state = viewModel.collectAsState().value

    Box(modifier = Modifier.fillMaxSize())
    {
        if (state.loading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
            )

            if (state.searchDisplayString?.isNotEmpty() == true) {
                Text(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 16.dp, end = 16.dp, bottom = 64.dp),
                    text = state.searchDisplayString,
                    color = Color.White,
                )
            }
        } else if (state.error) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = "Error",
            )
        }
    }
}