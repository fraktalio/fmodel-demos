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
import com.fraktalio.fmodel.example.domain.RestaurantCommand
import com.fraktalio.fmodel.example.domain.RestaurantEvent
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.getId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.axonframework.eventsourcing.eventstore.EventStore

/**
 * A convenient type alias for  EventRepository<RestaurantCommand?, RestaurantEvent?>
 */
typealias RestaurantAggregateEventStoreRepository = EventRepository<RestaurantCommand?, RestaurantEvent?>

internal open class RestaurantAggregateEventStoreRepositoryImpl(
    private val axonServerEventStore: EventStore
) : RestaurantAggregateEventStoreRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val axonServer = Dispatchers.IO.limitedParallelism(10)

    /**
     * Fetch events for the given command/this
     *
     * @return the [Flow] of [Event]s
     */
    override fun RestaurantCommand?.fetchEvents(): Flow<RestaurantEvent> =
        when (this) {
            is RestaurantCommand -> axonServerEventStore.fetchEvents<RestaurantEvent>(getId()).flowOn(axonServer)
            null -> emptyFlow()
        }

    /**
     * Save the event of type [Event] into the Axon Server
     *
     * @return the saved [Event]
     */
    override suspend fun RestaurantEvent?.save(): RestaurantEvent? =
        when (this) {
            is RestaurantEvent -> {
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
    override fun Flow<RestaurantEvent?>.save(): Flow<RestaurantEvent?> =
        flow {
            withContext(axonServer) {
                with(axonServerEventStore) {
                    val events = filterNotNull().toList()
                    publishEvents(events, lastSequenceNumber(events.first().getId()))
                }
            }
            emitAll(filterNotNull())
        }
}
