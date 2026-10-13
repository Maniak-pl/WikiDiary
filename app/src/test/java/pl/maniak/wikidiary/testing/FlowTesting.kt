package pl.maniak.wikidiary.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/** Collects the current value, runs [action] and returns the next emitted value. */
suspend fun <T> Flow<T>.nextValueAfter(action: suspend () -> Unit): T = coroutineScope {
    val values = Channel<T>(Channel.UNLIMITED)
    val collector = launch(Dispatchers.IO) { collect { values.send(it) } }
    withTimeout(5_000) { values.receive() }
    action()
    val next = withTimeout(5_000) { values.receive() }
    collector.cancel()
    next
}
