@file:Suppress("UNCHECKED_CAST")

package com.fraktalio.fmodel.example.eventsourcedsystem.command.adapter.persistence

import com.fraktalio.fmodel.application.LatestVersionProvider
import com.fraktalio.fmodel.example.domain.Event
import com.fraktalio.fmodel.example.eventsourcedsystem.command.adapter.getAggregateType
import com.fraktalio.fmodel.example.eventsourcedsystem.command.adapter.getId
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible
import org.axonframework.eventhandling.GenericDomainEventMessage
import org.axonframework.eventsourcing.eventstore.DomainEventStream
import org.axonframework.eventsourcing.eventstore.EventStore

private suspend fun EventStore.fetchEventStream(aggregateIdentifier: String): DomainEventStream =
    runInterruptible(currentCoroutineContext()) {
        readEvents(aggregateIdentifier)
    }

fun <E : Event> EventStore.fetchEvents(aggregateIdentifier: String): Flow<Pair<E, Long>> =
    flow {
        emitAll(fetchEventStream(aggregateIdentifier).asFlow().map { Pair(it.payload as E, it.sequenceNumber) })
    }

private suspend fun EventStore.publishEventMessages(events: List<GenericDomainEventMessage<*>>): Flow<GenericDomainEventMessage<*>> =
    runInterruptible(currentCoroutineContext()) {
        publish(events)
        events.asFlow()
    }

suspend fun EventStore.publishEvents(events: Flow<Event>, lastSequenceNumber: Long) = publishEventMessages(
    events
        .withIndex()
        .map { (index, event) ->
            GenericDomainEventMessage(
                event.getAggregateType(),
                event.getId(),
                lastSequenceNumber + index + 1,
                event
            )
        }
        .toList()
)

suspend fun EventStore.publishEvents(
    events: Flow<Event>,
    latestVersionProvider: LatestVersionProvider<Event?, Long>
): Flow<GenericDomainEventMessage<*>> =
    flow {
        val sequences: HashMap<String, Long> = hashMapOf()
        val indexes: HashMap<String, Long> = hashMapOf()

        val result = publishEventMessages(
            events
                .withIndex()
                .map { (index, event) ->
                    GenericDomainEventMessage(
                        event.getAggregateType(),
                        event.getId(),
                        sequences.computeIfAbsent(event.getId()) { runBlocking { latestVersionProvider(event).second } }
                                + indexes.compute(event.getId()) { _, v -> (v ?: -1) + 1 }!!
                                + 1,
                        event
                    )
                }
                .toList()
        )
        emitAll(result)
    }


suspend fun EventStore.lastSequenceNumber(aggregateIdentifier: String): Long =
    runInterruptible(currentCoroutineContext()) {
        lastSequenceNumberFor(aggregateIdentifier).orElse(-1)
    }

