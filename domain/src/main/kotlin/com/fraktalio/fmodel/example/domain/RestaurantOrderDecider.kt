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
    // Initial state of the Restaurant Order is `null`. It does not exist.
    initialState = null,
    // Exhaustive command handler(s): for each type of [RestaurantCommand] you are going to publish specific events/facts, as required by the current state/s of the [RestaurantOrder].
    decide = { c, s ->
        when (c) {
            is CreateRestaurantOrderCommand ->
                // ** positive flow **
                if (s == null) flowOf(RestaurantOrderCreatedEvent(c.identifier, c.lineItems, c.restaurantIdentifier))
                // ** negative flow 1 (publishing business error events) **
                else flowOf(
                    RestaurantOrderNotCreatedEvent(
                        c.identifier,
                        c.lineItems,
                        c.restaurantIdentifier,
                        "Restaurant order already exists"
                    )
                )
            //    ** negative flow 2 (publishing empty flow of events - ignoring negative flows - we are losing information :( ) **
            //  else emptyFlow()
            //    ** negative flow 3 (throwing exception - we are losing information - filtering exceptions is fragile) **
            //  else flow { throw RuntimeException("Restaurant order already exists") }
            is MarkRestaurantOrderAsPreparedCommand ->
                if ((s != null && CREATED == s.status)) flowOf(RestaurantOrderPreparedEvent(c.identifier))
                else flowOf(
                    RestaurantOrderNotPreparedEvent(
                        c.identifier,
                        "Restaurant order does not exist / not in CREATED status"
                    )
                )
            null -> emptyFlow() // We ignore the `null` command by emitting the empty flow. Only the Decider that can handle `null` command can be combined (Monoid) with other Deciders.
        }
    },
    // Exhaustive event-sourcing handler(s): for each event of type [RestaurantEvent] you are going to evolve from the current state/s of the [RestaurantOrder] to a new state of the [RestaurantOrder]
    evolve = { s, e ->
        when (e) {
            is RestaurantOrderCreatedEvent -> RestaurantOrder(e.identifier, e.restaurantId, CREATED, e.lineItems)
            is RestaurantOrderPreparedEvent ->
                if (s != null) RestaurantOrder(s.id, s.restaurantId, PREPARED, s.lineItems)
                else s
            is RestaurantOrderErrorEvent -> s
            null -> s // Null events are not changing the state / We return current state instead. Only the Decider that can handle `null` event can be combined (Monoid) with other Deciders.
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
        CREATED, PREPARED, REJECTED, CANCELLED
    }
}
