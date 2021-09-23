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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.axonframework.eventhandling.GenericDomainEventMessage
import org.axonframework.eventsourcing.eventstore.EventStore
import java.util.*


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
) : EventRepository<Command?, Event?> {

    /**
     * Fetch events for the given command/this
     *
     * @return the [Flow] of [Event]s
     */
    override fun Command?.fetchEvents(): Flow<Event?> =
        when (this) {
            is CreateRestaurantCommand,
            is ChangeRestaurantMenuCommand,
            is ActivateRestaurantMenuCommand,
            is PassivateRestaurantMenuCommand,
            is CreateRestaurantOrderCommand,
            is MarkRestaurantOrderAsPreparedCommand ->
                axonServerEventStore.readEvents(getId()).asFlow()
                    .map { it.payload as Event }
            is PlaceRestaurantOrderCommand ->
                flowOf(
                    axonServerEventStore.readEvents(getId()).asFlow(),
                    axonServerEventStore.readEvents(this.restaurantOrderIdentifier.identifier.toString()).asFlow()
                ).flattenConcat()
                    .map { it.payload as Event }
            null -> emptyFlow<Event>()
        }


    /**
     * Save the event of type [Event] into the Axon Server
     *
     * @return the saved [Event]
     */
    override suspend fun Event?.save(): Event? =
        when (this) {
            is Event -> {
                axonServerEventStore.publish(
                    GenericDomainEventMessage(
                        getAggregateType(),
                        getId(),
                        axonServerEventStore.lastSequenceNumberFor(getId())
                            .orElse(-1) + 1,
                        this
                    )
                )
                this
            }
            null -> null
        }

    /**
     * Save the events of type [Event] into the Axon Server
     *
     * @return the [Flow] of saved [Event]s
     */
    override fun Flow<Event?>.save(): Flow<Event?> =
        runBlocking {

            val restaurantEvent = this@save.filterIsInstance<RestaurantEvent>().firstOrNull()
            val restaurantOrderEvent = this@save.filterIsInstance<RestaurantOrderEvent>().firstOrNull()

            val lastRestaurantSequenceNumber =
                if (restaurantEvent != null) axonServerEventStore.lastSequenceNumberFor(restaurantEvent.getId()) else Optional.empty()
            val lastRestaurantOrderSequenceNumber =
                if (restaurantOrderEvent != null) axonServerEventStore.lastSequenceNumberFor(restaurantOrderEvent.getId()) else Optional.empty()

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
                                    lastRestaurantSequenceNumber.orElse(-1) + ++restaurantIndex,
                                    event
                                )
                            is RestaurantOrderEvent ->
                                GenericDomainEventMessage(
                                    event.getAggregateType(),
                                    event.getId(),
                                    lastRestaurantOrderSequenceNumber.orElse(-1) + ++restaurantOrderIndex,
                                    event
                                )
                        }
                    }
            )
            this@save.filterNotNull()
        }
}
