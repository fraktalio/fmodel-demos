package com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.persistence

import com.fraktalio.fmodel.application.EventRepository
import com.fraktalio.fmodel.example.domain.Command
import com.fraktalio.fmodel.example.domain.Event
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.getId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import org.axonframework.eventsourcing.eventstore.EventStore

internal open class AggregateEventStoreRepositoryImpl<C : Command?, E : Event?>(
    private val axonServerEventStore: EventStore
) : EventRepository<C, E> {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val axonServer = Dispatchers.IO.limitedParallelism(10)

    /**
     * Fetch events for the given command/this
     *
     * @return the [Flow] of [Event]s
     */
    override fun C.fetchEvents(): Flow<E> =
        when (this) {
            is Command -> axonServerEventStore
                .fetchEvents<E>(getId())
                .flowOn(axonServer)

            else -> emptyFlow()
        }

    /**
     * Save the events of type [Event] into the Axon Server
     *
     * @return the [Flow] of saved [Event]s
     */
    override fun Flow<E>.save(): Flow<E> =
        flow {
            with(axonServerEventStore) {
                val events = filterNotNull().toList()
                publishEvents(events, lastSequenceNumber(events.first().getId()))
            }
            emitAll(filterNotNull())
        }.flowOn(axonServer)
}