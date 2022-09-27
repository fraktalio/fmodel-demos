/*
 * Copyright (c) 2021 Fraktalio D.O.O. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fraktalio.fmodel.example.eventsourcedsystem.command.adapter.persistence

import com.fraktalio.fmodel.application.EventRepository
import com.fraktalio.fmodel.example.domain.*
import com.fraktalio.fmodel.example.eventsourcedsystem.command.adapter.getAggregateType
import com.fraktalio.fmodel.example.eventsourcedsystem.command.adapter.getId
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.axonframework.eventhandling.GenericDomainEventMessage
import org.axonframework.eventsourcing.eventstore.EventStore

/**
 * A convenient type alias for EventRepository<Command?, Event?>
 */
typealias AggregateEventStoreRepository = EventRepository<Command?, Event?>

/**
 * Event repository/store implementation
 *
 * It is using Axon Server as an Event Store, to durably store events
 *
 * @property axonServerEventStore Axon EventStore
 * @constructor Create empty Aggregate repository impl
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
internal open class AggregateEventStoreRepositoryImpl(
    private val axonServerEventStore: EventStore
) : AggregateEventStoreRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val axonServer = Dispatchers.IO.limitedParallelism(10)

    /**
     * Fetch events for the given command/this
     *
     * @return the [Flow] of [Event]s
     */
    override fun Command?.fetchEvents(): Flow<Event> =
        when (this) {
            is Command -> axonServerEventStore.fetchEvents<Event>(getId()).flowOn(axonServer)
            null -> emptyFlow()
        }


    /**
     * Save the event of type [Event] into the Axon Server
     *
     * @return the saved [Event]
     */
    override suspend fun Event?.save(): Event? =
        when (this) {
            is Event -> {
                withContext(axonServer) {
                    with(axonServerEventStore) {
                        publishEvents(listOf(this@save), lastSequenceNumber(this@save.getId()))
                    }
                }
                this
            }
            null -> null
        }


    /**
     * Save the events of type [Event] into the Axon Server
     *
     * @return the [Flow] of saved [Event]s
     */
    override fun Flow<Event?>.save(): Flow<Event> =
        runBlocking {
            val restaurantEvent = this@save.filterIsInstance<RestaurantEvent>().firstOrNull()
            val lastRestaurantSequenceNumber =
                if (restaurantEvent != null) axonServerEventStore.lastSequenceNumber(restaurantEvent.getId()) else -1
            val restaurantOrderEvent = this@save.filterIsInstance<RestaurantOrderEvent>().firstOrNull()
            val lastRestaurantOrderSequenceNumber =
                if (restaurantOrderEvent != null) axonServerEventStore.lastSequenceNumber(restaurantOrderEvent.getId()) else -1

            var restaurantIndex = 0
            var restaurantOrderIndex = 0

            axonServerEventStore.publish(
                this@save.filterNotNull().toList()
                    .map { event ->
                        when (event) {
                            is RestaurantEvent ->
                                GenericDomainEventMessage(
                                    event.getAggregateType(),
                                    event.getId(),
                                    lastRestaurantSequenceNumber + ++restaurantIndex,
                                    event
                                )
                            is RestaurantOrderEvent ->
                                GenericDomainEventMessage(
                                    event.getAggregateType(),
                                    event.getId(),
                                    lastRestaurantOrderSequenceNumber + ++restaurantOrderIndex,
                                    event
                                )
                        }
                    }
            )
            this@save.filterNotNull()
        }
}
