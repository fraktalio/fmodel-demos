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

import com.fraktalio.fmodel.application.EventLockingRepository
import com.fraktalio.fmodel.application.LatestVersionProvider
import com.fraktalio.fmodel.example.domain.*
import com.fraktalio.fmodel.example.eventsourcedsystem.command.adapter.getId
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.axonframework.eventsourcing.eventstore.EventStore

/**
 * A convenient type alias for EventRepository<Command?, Event?>
 */
typealias AggregateEventStoreLockingRepository = EventLockingRepository<Command?, Event?, Long>

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
internal open class AggregateEventStoreLockingRepositoryImpl(
    private val axonServerEventStore: EventStore
) : AggregateEventStoreLockingRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val axonServer = Dispatchers.IO.limitedParallelism(10)

    override fun Command?.fetchEvents(): Flow<Pair<Event, Long>> =
        when (this) {
            is Command -> axonServerEventStore.fetchEvents<Event>(getId()).flowOn(axonServer)
            null -> emptyFlow()
        }

    override val latestVersionProvider: LatestVersionProvider<Event?, Long> =
        { Pair(it, it?.let { it1 -> axonServerEventStore.lastSequenceNumber(it1.identifierString) } ?: -1) }

    override fun Flow<Event?>.save(latestVersion: Pair<Event?, Long>?): Flow<Pair<Event, Long>> =
        flow {
            emitAll(axonServerEventStore.publishEvents(filterNotNull(), latestVersion?.second ?: -1)
                .map { Pair(it.payload as Event, it.sequenceNumber) }
            )
        }.flowOn(axonServer)

    override fun Flow<Event?>.save(latestVersionProvider: LatestVersionProvider<Event?, Long>): Flow<Pair<Event, Long>> =
        flow {
            emitAll(axonServerEventStore.publishEvents(filterNotNull(), latestVersionProvider)
                .map { Pair(it.payload as Event, it.sequenceNumber) }
            )
        }.flowOn(axonServer)
}


