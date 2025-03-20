package com.example.wazesearcher.wazeshare

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class WazeShareViewModel : ContainerHost<WazeShareState, WazeShareSideEffect>, ViewModel() {
    override val container = container<WazeShareState, WazeShareSideEffect>(WazeShareState())

    fun loading() = intent {
        reduce {
            state.copy(
                loading = true,
                error = false,
            )
        }
    }

    fun error() = intent {
        reduce {
            state.copy(
                loading = false,
                error = true,
            )
        }
    }

    fun updateUrl(url: String) = intent {
        reduce {
            state.copy(
                url = url,
            )
        }
    }

    fun retry() = intent {
        postSideEffect(WazeShareSideEffect.Retry)
    }
}