package com.me.firebaseauthsample.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resumeWithException

/**
 * Awaits for completion of the task without blocking a thread.
 *
 * This suspending function is cancellable.
 * If the [Job] of the current coroutine is cancelled or completed while this suspending function is waiting, this function
 * stops waiting for the completion stage and immediately resumes with [CancellationException].
 */
@Suppress("UNCHECKED_CAST")
suspend fun <T> Task<T>.await(): T {
    // fast path
    if (isComplete) {
        val e = exception
        return if (e == null) {
            if (isCanceled) {
                throw CancellationException("Task $this was cancelled normally.")
            } else {
                @Suppress("UNCHECKED_CAST")
                result as T
            }
        } else {
            throw e
        }
    }

    return suspendCancellableCoroutine { cont ->
        addOnCompleteListener {
            try {
                val e = exception
                if (e == null) {
                    if (isCanceled) cont.cancel() else cont.resume(value = result as T, null)
                } else {
                    cont.resumeWithException(e)
                }
            } catch (e: Throwable) {
                cont.resumeWithException(e)
            }
        }
    }
}

fun <E> SendChannel<E>.safeSendBlocking(element: E) {
    // fast path
    if (safeOffer(element))
        return
    // slow path
    runBlocking {
        safeOffer(element)
    }
}

fun <T> SendChannel<T>.safeOffer(value: T): Boolean {
    return try {
        offer(value)
    } catch (e: Throwable) {
        println("ChannelException: error when emit ${e.message}")
        false
    }
}

fun <T> Flow<T>.asLiveData(scope: CoroutineScope): LiveData<T> {
    val liveData = MutableLiveData<T>()
    scope.launch {
        collect {
            liveData.value = it
        }
    }
    return liveData
}
