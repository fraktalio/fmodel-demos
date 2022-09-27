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

package com.fraktalio.fmodel.example.eventsourcedsystem.command.application

import com.fraktalio.fmodel.application.EventSourcingOrchestratingAggregate
import com.fraktalio.fmodel.application.eventSourcingOrchestratingAggregate
import com.fraktalio.fmodel.domain.combine
import com.fraktalio.fmodel.example.domain.*
import com.fraktalio.fmodel.example.eventsourcedsystem.command.adapter.persistence.AggregateEventStoreRepository
import kotlinx.coroutines.FlowPreview

/**
 * A convenient type alias for EventSourcingAggregate<Command?, Pair<RestaurantOrder?, Restaurant?>, Event?>
 */
typealias Aggregate = EventSourcingOrchestratingAggregate<Command?, Pair<RestaurantOrder?, Restaurant?>, Event?>

/**
 * One, big aggregate that is `combining` all deciders: [restaurantOrderDecider], [restaurantDecider].
 * Every command will be handled by one of the deciders.
 * The decider that is not interested in specific command type will simply ignore it (do nothing).
 *
 * @param restaurantOrderDecider restaurantOrderDecider is used internally to handle commands and produce new events.
 * @param restaurantDecider restaurantDecider is used internally to handle commands and produce new events.
 * @param restaurantOrderSaga restaurantOrderSaga is used internally to react on [RestaurantEvent]s and produce commands of type [RestaurantOrderCommand]
 * @param restaurantSaga restaurantSaga is used internally to react on [RestaurantOrderEvent]s and produce commands of type [RestaurantCommand]
 * @param eventRepository is used to store the newly produced events of the Restaurant and/or Restaurant order together
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@OptIn(FlowPreview::class)
internal fun aggregate(
    restaurantOrderDecider: RestaurantOrderDecider,
    restaurantDecider: RestaurantDecider,
    restaurantOrderSaga: RestaurantOrderSaga,
    restaurantSaga: RestaurantSaga,
    eventRepository: AggregateEventStoreRepository
): Aggregate = eventSourcingOrchestratingAggregate(
    // Combining two deciders into one.
    decider = restaurantOrderDecider.combine(restaurantDecider),
    // How and where do you want to store new events.
    eventRepository = eventRepository,
    // Combining individual choreography Sagas into one orchestrating Saga.
    saga = restaurantOrderSaga.combine(restaurantSaga)
)



