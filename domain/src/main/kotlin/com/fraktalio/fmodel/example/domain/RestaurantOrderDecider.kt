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

package com.fraktalio.fmodel.example.domain

import com.fraktalio.fmodel.domain.Decider
import com.fraktalio.fmodel.example.domain.RestaurantOrder.Status.CREATED
import com.fraktalio.fmodel.example.domain.RestaurantOrder.Status.PREPARED
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Decider is a pure domain component.
 * Decider is a datatype that represents the main decision-making algorithm.
 *
 * `decide / command handlers` is a pure function/lambda that takes any command of type [RestaurantOrderCommand] and input state of type [RestaurantOrder] as parameters, and returns the flow of output events Flow<[RestaurantOrderEvent]> as a result
 * `evolve / event-sourcing handlers` is a pure function/lambda that takes input state of type [RestaurantOrder] and input event of type [RestaurantOrderEvent] as parameters, and returns the output/new state [RestaurantOrder]
 * `initialState` is a starting point / An initial state of [RestaurantOrder]. In our case, it is `null`
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun restaurantOrderDecider() = Decider<RestaurantOrderCommand?, RestaurantOrder?, RestaurantOrderEvent?>(
    initialState = null,
    // Command handling part: for each type of [RestaurantCommand] you are going to publish specific events/facts, as required.
    decide = { c, s ->
        when {
            (c is CreateRestaurantOrderCommand) && (s == null) -> flowOf(
                RestaurantOrderCreatedEvent(
                    c.identifier,
                    c.lineItems,
                    c.restaurantIdentifier
                )
            )
            (c is MarkRestaurantOrderAsPreparedCommand) && (s != null) && (CREATED == s.status) -> flowOf(
                RestaurantOrderPreparedEvent(
                    c.identifier
                )
            )
            else -> emptyFlow()

        }
    },
    // Event-sourcing handling part: for each event of type [RestaurantEvent] you are going to evolve to a new state of the [Restaurant]
    evolve = { s, e ->
        when {
            (e is RestaurantOrderCreatedEvent) -> RestaurantOrder(
                e.identifier,
                e.restaurantId,
                CREATED,
                e.lineItems
            )
            (e is RestaurantOrderPreparedEvent) && (s != null) -> RestaurantOrder(
                s.id,
                s.restaurantId,
                PREPARED,
                s.lineItems
            )
            else -> s

        }
    }
)

/**
 * A model of the RestaurantOrder / It represents the state of the Restaurant Order.
 *
 * @property id
 * @property restaurantId
 * @property status
 * @property lineItems
 * @constructor Creates [RestaurantOrder]
 */
data class RestaurantOrder(
    val id: RestaurantOrderId,
    val restaurantId: RestaurantId,
    val status: Status,
    val lineItems: List<RestaurantOrderLineItem>
) {
    enum class Status {
        CREATED, PREPARED, CANCELLED
    }
}
