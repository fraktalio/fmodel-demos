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

package com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.persistence

import com.fraktalio.fmodel.application.EventRepository
import com.fraktalio.fmodel.example.domain.Event
import com.fraktalio.fmodel.example.domain.RestaurantOrderCommand
import com.fraktalio.fmodel.example.domain.RestaurantOrderEvent
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.getAggregateType
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.getId
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.axonframework.eventhandling.GenericDomainEventMessage
import org.axonframework.eventsourcing.eventstore.EventStore
import java.util.*


internal open class RestaurantOrderAggregateEventStoreRepositoryImpl(
    private val axonServerEventStore: EventStore
) : EventRepository<RestaurantOrderCommand?, RestaurantOrderEvent?> {

    /**
     * Fetch events for the given command/this
     *
     * @return the [Flow] of [Event]s
     */
    override fun RestaurantOrderCommand?.fetchEvents(): Flow<RestaurantOrderEvent?> =
        when (this) {
            is RestaurantOrderCommand ->
                axonServerEventStore.readEvents(getId()).asFlow().map { it.payload as RestaurantOrderEvent }
            null -> emptyFlow<RestaurantOrderEvent>()
        }


    /**
     * Save the event of type [Event] into the Axon Server
     *
     * @return the saved [Event]
     */
    override suspend fun RestaurantOrderEvent?.save(): RestaurantOrderEvent? =
        when (this) {
            is RestaurantOrderEvent -> {
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
    override fun Flow<RestaurantOrderEvent?>.save(): Flow<RestaurantOrderEvent?> =
        runBlocking {

            val event = this@save.filterIsInstance<RestaurantOrderEvent>().firstOrNull()

            val lastSequenceNumber =
                if (event != null) axonServerEventStore.lastSequenceNumberFor(event.getId()) else Optional.empty()

            var index = 0

            axonServerEventStore.publish(
                this@save
                    .filterNotNull()
                    .toList()
                    .map {
                        GenericDomainEventMessage(
                            it.getAggregateType(),
                            it.getId(),
                            lastSequenceNumber.orElse(-1) + ++index,
                            it
                        )
                    }
            )
            this@save.filterNotNull()
        }
}
