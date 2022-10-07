package com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.persistence

import com.fraktalio.fmodel.example.domain.Event
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.getAggregateType
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.getId
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runInterruptible
import org.axonframework.eventhandling.EventMessage
import org.axonframework.eventhandling.GenericDomainEventMessage
import org.axonframework.eventsourcing.eventstore.DomainEventStream
import org.axonframework.eventsourcing.eventstore.EventStore

private suspend fun EventStore.fetchEventStream(aggregateIdentifier: String): DomainEventStream =
    runInterruptible(currentCoroutineContext()) {
        readEvents(aggregateIdentifier)
    }

@Suppress("UNCHECKED_CAST")
fun <E : Event?> EventStore.fetchEvents(aggregateIdentifier: String): Flow<E> =
    flow {
        emitAll(fetchEventStream(aggregateIdentifier).asFlow().map { it.payload as E })
    }

private suspend fun EventStore.publishEventMessages(events: List<EventMessage<*>>): Unit =
    runInterruptible(currentCoroutineContext()) {
        publish(events)
    }

suspend fun EventStore.publishEvents(events: List<Event>, lastSequenceNumber: Long) = publishEventMessages(
    events.mapIndexed { index, event ->
        GenericDomainEventMessage(
            event.getAggregateType(),
            event.getId(),
            lastSequenceNumber + index + 1,
            event
        )
    })

suspend fun EventStore.lastSequenceNumber(aggregateIdentifier: String): Long =
    runInterruptible(currentCoroutineContext()) {
        lastSequenceNumberFor(aggregateIdentifier).orElse(-1)
    }

