package com.fraktalio.fmodel.example.eventsourcedsystem.query.adapter.web

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runInterruptible
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway

suspend inline fun <reified R, reified Q : Any> QueryGateway.queryMany(query: Q): List<R> =
    runInterruptible(currentCoroutineContext()) {
        this@queryMany.query(
            query,
            ResponseTypes.multipleInstancesOf(R::class.java)
        ).get()
    }

inline fun <reified R, reified Q : Any> QueryGateway.queryFlow(query: Q): Flow<R> =
    flow {
        emitAll(
            runInterruptible(currentCoroutineContext()) {
                this@queryFlow.query(
                    query,
                    ResponseTypes.multipleInstancesOf(R::class.java)
                ).get().asFlow()
            }
        )
    }

