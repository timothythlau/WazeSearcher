package com.example.wazesearcher

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.MalformedURLException
import java.net.URL
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> Task<T>.await(): T {
    // If the task is already complete, it is no longer a suspend operation.
    if (isComplete) {
        val e = exception
        return if (e == null) {
            if (isCanceled) {
                throw CancellationException("Task $this was cancelled normally.")
            } else {
                result!!
            }
        } else {
            throw e
        }
    }
    return suspendCancellableCoroutine { continuation ->
        addOnCompleteListener {
            val e = exception
            if (e == null) {
                if (isCanceled) {
                    continuation.cancel()
                } else {
                    continuation.resume(result!!)
                }
            } else {
                continuation.resumeWithException(e)
            }
        }
    }
}

fun String.isValidUrl(): Boolean {
    return try {
        URL(this)
        true
    } catch (e: MalformedURLException) {
        false
    }
}