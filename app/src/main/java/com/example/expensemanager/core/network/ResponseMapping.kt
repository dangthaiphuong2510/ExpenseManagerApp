package com.example.expensemanager.core.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

fun <T> flowTransform(block: suspend FlowCollector<T>.() -> T): Flow<T> = flow {
    try {
        emit(block())
    } catch (e: Throwable) {
        throw e.mapNetworkError()
    }
}
