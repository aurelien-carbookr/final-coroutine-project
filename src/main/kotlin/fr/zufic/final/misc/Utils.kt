package fr.zufic.final.misc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T> Flow<T>.distinct(): Flow<T> = flow {
    val previous: MutableSet<T> = mutableSetOf()
    collect {
        if (previous.add(it)) {
            emit(it)
        }
    }
}