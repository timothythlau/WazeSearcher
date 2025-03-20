package com.example.wazesearcher.wazeshare

data class WazeShareState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val searchDisplayString: String? = null,
)

sealed class WazeShareSideEffect {
    data object Retry : WazeShareSideEffect()
}