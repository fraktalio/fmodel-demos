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

import com.fraktalio.fmodel.domain.View
import com.fraktalio.fmodel.example.domain.RestaurantOrderView.Status.CREATED
import com.fraktalio.fmodel.example.domain.RestaurantOrderView.Status.PREPARED
import com.fraktalio.fmodel.example.domain.RestaurantView.Status

/**
 * RestaurantOrder View is a datatype that represents the event handling algorithm,
 * responsible for translating the events into denormalized state,
 * which is more adequate for querying.
 *
 * `evolve / event handlers` is a pure function/lambda that takes input state of type [RestaurantOrderView] and input event of type [RestaurantOrderEvent] as parameters, and returns the output/new state [RestaurantOrderView]
 * `initialState` is a starting point / An initial state of [RestaurantOrderView]. In our case, it is `null`
 */
fun restaurantOrderView() = View<RestaurantOrderView?, RestaurantOrderEvent?>(
    // Initial state of the [RestaurantOrderView] is `null`. It does not exist.
    initialState = null,
    // Exhaustive event-sourcing handling part: for each event of type [RestaurantOrderEvent] you are going to evolve from the current state/s of the [RestaurantOrderView] to a new state of the [RestaurantOrderView].
    evolve = { s, e ->
        when (e) {
            is RestaurantOrderCreatedEvent -> RestaurantOrderView(e.identifier, e.restaurantId, CREATED, e.lineItems)
            is RestaurantOrderPreparedEvent ->
                if (s != null) RestaurantOrderView(s.id, s.restaurantId, PREPARED, s.lineItems)
                else s
            is RestaurantOrderErrorEvent -> s
            null -> s // We ignore the `null` event by returning current State/s. Only the View that can handle `null` event can be combined (Monoid) with other Views.

        }
    }
)

/**
 * A model of the RestaurantOrderView / Projection
 *
 * @property id
 * @property restaurantId
 * @property status
 * @property lineItems
 * @constructor Creates [RestaurantOrderView]
 */
data class RestaurantOrderView(
    val id: RestaurantOrderId,
    val restaurantId: RestaurantId,
    val status: Status,
    val lineItems: List<RestaurantOrderLineItem>
) {
    enum class Status {
        CREATED, PREPARED, CANCELLED
    }
}
