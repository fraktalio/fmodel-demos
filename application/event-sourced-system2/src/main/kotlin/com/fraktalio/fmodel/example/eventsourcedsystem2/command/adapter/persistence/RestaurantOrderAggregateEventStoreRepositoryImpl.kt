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
import com.fraktalio.fmodel.example.domain.*
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.getAggregateType
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.getId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.axonframework.eventhandling.GenericDomainEventMessage
import org.axonframework.eventsourcing.eventstore.EventStore
import java.util.*

/**
 * A convenient type alias for EventRepository<RestaurantOrderCommand?, RestaurantOrderEvent?>
 */
typealias RestaurantOrderAggregateEventStoreRepository = EventRepository<RestaurantOrderCommand?, RestaurantOrderEvent?>


internal open class RestaurantOrderAggregateEventStoreRepositoryImpl(
    private val axonServerEventStore: EventStore
) : RestaurantOrderAggregateEventStoreRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val axonServer = Dispatchers.IO.limitedParallelism(10)

    /**
     * Fetch events for the given command/this
     *
     * @return the [Flow] of [Event]s
     */
    override fun RestaurantOrderCommand?.fetchEvents(): Flow<RestaurantOrderEvent?> =
        flow {
            emitAll(
                when (this@fetchEvents) {
                    is RestaurantOrderCommand ->
                        withContext(axonServer) {
                            axonServerEventStore.fetchEvents(getId())
                        }
                    null -> emptyFlow()
                }
            )
        }


    /**
     * Save the event of type [Event] into the Axon Server
     *
     * @return the saved [Event]
     */
    override suspend fun RestaurantOrderEvent?.save(): RestaurantOrderEvent? =
        when (this) {
            is RestaurantOrderEvent -> {
                withContext(axonServer) {
                    axonServerEventStore.publishEvents(listOf(this@save))
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
    override fun Flow<RestaurantOrderEvent?>.save(): Flow<RestaurantOrderEvent?> =
        flow {
            withContext(axonServer) {
                axonServerEventStore.publishEvents(filterNotNull().toList())
            }
            emitAll(filterNotNull())
        }
}
