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

package com.fraktalio.fmodel.example.eventsourcedsystem2.command.application

import com.fraktalio.fmodel.application.EventRepository
import com.fraktalio.fmodel.application.EventSourcingAggregate
import com.fraktalio.fmodel.application.eventSourcingAggregate
import com.fraktalio.fmodel.example.domain.RestaurantOrder
import com.fraktalio.fmodel.example.domain.RestaurantOrderCommand
import com.fraktalio.fmodel.example.domain.RestaurantOrderDecider
import com.fraktalio.fmodel.example.domain.RestaurantOrderEvent
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.persistence.RestaurantOrderAggregateEventStoreRepository

/**
 * A convenient type alias for EventSourcingAggregate<RestaurantOrderCommand?, RestaurantOrder?, RestaurantOrderEvent?>
 */
typealias RestaurantOrderAggregate = EventSourcingAggregate<RestaurantOrderCommand?, RestaurantOrder?, RestaurantOrderEvent?>

/**
 * Restaurant Order Aggregate
 *
 * @param restaurantOrderDecider restaurantOrderDecider is used internally to handle commands and produce new events.
 * @param eventRepository is used to store the newly produced events of the Restaurant Order.
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
internal fun restaurantOrderAggregate(
    restaurantOrderDecider: RestaurantOrderDecider,
    eventRepository: EventRepository<RestaurantOrderCommand?, RestaurantOrderEvent?>
): RestaurantOrderAggregate = eventSourcingAggregate(
    decider = restaurantOrderDecider,
    eventRepository = eventRepository,
)



