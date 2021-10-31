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
import com.fraktalio.fmodel.example.domain.RestaurantViewState.*
import com.fraktalio.fmodel.example.domain.RestaurantViewState.MenuStatus.ACTIVE
import com.fraktalio.fmodel.example.domain.RestaurantViewState.MenuStatus.PASSIVE
import java.util.*
import java.util.stream.Collectors

/**
 * A convenient type alias for View<RestaurantViewState?, RestaurantEvent?>
 */
typealias RestaurantView = View<RestaurantViewState?, RestaurantEvent?>

/**
 * Restaurant View is a datatype that represents the event handling algorithm,
 * responsible for translating the events into denormalized state,
 * which is more adequate for querying.
 *
 * `evolve / event handlers` is a pure function/lambda that takes input state of type [RestaurantViewState] and input event of type [RestaurantEvent] as parameters, and returns the output/new state [RestaurantViewState]
 * `initialState` is a starting point / An initial state of [RestaurantViewState]. In our case, it is `null`
 */
fun restaurantView() = RestaurantView(
    // Initial state of the [RestaurantViewState] is `null`. It does not exist.
    initialState = null,
    // Exhaustive event-sourcing handling part: for each event of type [RestaurantEvent] you are going to evolve from the current state/s of the [RestaurantViewState] to a new state of the [RestaurantViewState].
    evolve = { s, e ->
        when (e) {
            is RestaurantCreatedEvent -> RestaurantViewState(
                e.identifier,
                e.name,
                RestaurantMenu(
                    e.menu.menuId,
                    e.menu.menuItems.map { MenuItem(it.id, it.menuItemId, it.name, it.price) },
                    e.menu.cuisine
                ),
                Status.OPEN
            )
            is RestaurantMenuChangedEvent ->
                if (s != null) RestaurantViewState(
                    s.id,
                    s.name,
                    RestaurantMenu(
                        e.menu.menuId,
                        e.menu.menuItems.map { MenuItem(it.id, it.menuItemId, it.name, it.price) },
                        e.menu.cuisine
                    ), s.status
                )
                else s
            is RestaurantMenuActivatedEvent ->
                if (s != null) RestaurantViewState(
                    s.id,
                    s.name,
                    RestaurantMenu(
                        s.menu.menuId,
                        s.menu.items,
                        s.menu.cuisine,
                        ACTIVE
                    ), s.status
                )
                else s
            is RestaurantMenuPassivatedEvent ->
                if (s != null) RestaurantViewState(
                    s.id,
                    s.name,
                    RestaurantMenu(
                        s.menu.menuId,
                        s.menu.items,
                        s.menu.cuisine,
                        PASSIVE
                    ), s.status
                )
                else s
            is RestaurantOrderPlacedAtRestaurantEvent -> s
            is RestaurantErrorEvent -> s
            null -> s // We ignore the `null` event by returning current State/s. Only the View that can handle `null` event can be combined (Monoid) with other Views.
        }
    }
)

/**
 * A RestaurantViewState / projection
 *
 * @property id A unique identifier
 * @property name A name of the restaurant
 * @property menu Current [RestaurantMenu] of the restaurant
 * @property status A status/state  [Status] of the restaurant: OPEN, CLOSED, SHUTDOWN
 * @constructor Creates [RestaurantViewState]
 */
data class RestaurantViewState(
    val id: RestaurantId,
    val name: String,
    val menu: RestaurantMenu,
    val status: Status
) {


    fun isValid(command: PlaceRestaurantOrderCommand): Boolean =
        (ACTIVE == menu.status) && (menu.items.stream().map { mi -> mi.id }.collect(Collectors.toList())
            .containsAll(command.lineItems.stream().map { li -> li.menuItemId }.collect(Collectors.toList())))


    enum class Status {
        OPEN,
        CLOSED,
        SHUTDOWN
    }

    data class MenuItem(val id: String, val menuItemId: String, val name: String, val price: Money)

    enum class MenuStatus {
        ACTIVE,
        PASSIVE
    }

    data class RestaurantMenu(
        val menuId: UUID,
        val items: List<MenuItem>,
        val cuisine: RestaurantMenuCuisine,
        val status: MenuStatus = ACTIVE
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as RestaurantMenu

            if (menuId != other.menuId) return false
            if (items.equals(other.items)) return false
            if (cuisine != other.cuisine) return false
            if (status != other.status) return false

            return true
        }

        override fun hashCode(): Int {
            var result = menuId.hashCode()
            result = 31 * result + items.hashCode()
            result = 31 * result + cuisine.hashCode()
            result = 31 * result + status.hashCode()
            return result
        }
    }
}
