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
import com.fraktalio.fmodel.example.domain.Restaurant.RestaurantMenu
import com.fraktalio.fmodel.example.domain.Restaurant.Status
import java.util.*
import java.util.stream.Collectors

/**
 * Decider is a pure domain component.
 * Decider is a datatype that represents the main decision making algorithm.
 *
 * This function is responsible of creating one.
 *
 * `decide / command handlers` is a pure function/lambda that takes any command of type [RestaurantCommand] and input state of type [Restaurant] as parameters, and returns the iterable of output events [Iterable]<[RestaurantEvent]> as a result
 * `evolve / event-sourcing handlers` is a pure function/lambda that takes input state of type [Restaurant] and input event of type [RestaurantEvent] as parameters, and returns the output/new state [Restaurant]
 * `initialState` is a starting point / An initial state of [Restaurant]. In our case, it is `null`
 * `isTerminal` is a pure function/lambda that takes current state of [Restaurant], and returns [Boolean] showing if the current state is terminal/final. No commands can be handled once in this state.
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun restaurantDecider() = Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>(
    initialState = null,
    isTerminal = { s -> s != null && Restaurant.Status.SHUTDOWN == s.status },
    // Command handling part: for each type of [RestaurantCommand] you are going to publish specific events/facts, as required.
    decide = { c, s ->
        when {
            (c is CreateRestaurantCommand) && (s == null) -> listOf(
                RestaurantCreatedEvent(
                    c.identifier,
                    c.name,
                    c.menu
                )
            )
            (c is ChangeRestaurantMenuCommand) && (s != null) -> listOf(
                RestaurantMenuChangedEvent(
                    c.identifier,
                    c.menu
                )
            )
            (c is ActivateRestaurantMenuCommand) && (s != null) -> listOf(
                RestaurantMenuActivatedEvent(
                    c.identifier,
                    c.menuId
                )
            )
            (c is PassivateRestaurantMenuCommand) && (s != null) -> listOf(
                RestaurantMenuPassivatedEvent(
                    c.identifier,
                    c.menuId
                )
            )
            (c is PlaceRestaurantOrderCommand) && (s != null) && s.isValid(c) -> listOf(
                RestaurantOrderPlacedAtRestaurantEvent(
                    c.identifier,
                    c.lineItems,
                    c.restaurantOrderIdentifier
                )
            )
            (c is PlaceRestaurantOrderCommand) && (s != null) && !s.isValid(c) -> listOf(
                RestaurantOrderRejectedByRestaurantEvent(
                    c.identifier,
                    c.restaurantOrderIdentifier,
                    "Not on the menu"
                )
            )
            else -> emptyList()
        }
    },
    // Event-sourcing handling part: for each event of type [RestaurantEvent] you are going to evolve to a new state of the [Restaurant]
    evolve = { s, e ->
        when {
            (e is RestaurantCreatedEvent) -> Restaurant(
                e.identifier,
                e.name,
                Restaurant.RestaurantMenu(
                    e.menu.menuId,
                    e.menu.menuItems.map { Restaurant.MenuItem(it.id, it.name, it.price) },
                    e.menu.cuisine
                ),
                Restaurant.Status.OPEN
            )
            (e is RestaurantMenuChangedEvent) && (s != null) -> Restaurant(
                s.id,
                s.name,
                Restaurant.RestaurantMenu(
                    e.menu.menuId,
                    e.menu.menuItems.map { Restaurant.MenuItem(it.id, it.name, it.price) },
                    e.menu.cuisine
                ), s.status
            )
            (e is RestaurantMenuActivatedEvent) && (s != null) -> Restaurant(
                s.id,
                s.name,
                Restaurant.RestaurantMenu(
                    s.menu.menuId,
                    s.menu.items,
                    s.menu.cuisine,
                    Restaurant.MenuStatus.ACTIVE
                ), s.status
            )
            (e is RestaurantMenuPassivatedEvent) && (s != null) -> Restaurant(
                s.id,
                s.name,
                Restaurant.RestaurantMenu(
                    s.menu.menuId,
                    s.menu.items,
                    s.menu.cuisine,
                    Restaurant.MenuStatus.PASSIVE
                ), s.status
            )
            else -> s
        }
    }
)

/**
 * A model of the Restaurant / It represents the state of the Restaurant.
 *
 * @property id A unique identifier
 * @property name A name of the restaurant
 * @property menu Current [RestaurantMenu] of the restaurant
 * @property status A status/state  [Status] of the restaurant: OPEN, CLOSED, SHUTDOWN
 * @constructor Creates Restaurant
 */
data class Restaurant(
    val id: RestaurantId,
    val name: String,
    val menu: RestaurantMenu,
    val status: Status
) {
    fun isValid(command: PlaceRestaurantOrderCommand): Boolean =
        (MenuStatus.ACTIVE == menu.status) && (menu.items.stream().map { mi -> mi.id }.collect(Collectors.toList())
            .containsAll(command.lineItems.stream().map { li -> li.menuItemId }.collect(Collectors.toList())))


    enum class Status {
        OPEN,
        CLOSED,
        SHUTDOWN
    }

    data class MenuItem(val id: String, val name: String, val price: Money)

    enum class MenuStatus {
        ACTIVE,
        PASSIVE
    }

    data class RestaurantMenu(
        val menuId: UUID,
        val items: List<MenuItem>,
        val cuisine: RestaurantMenuCuisine,
        val status: MenuStatus = MenuStatus.ACTIVE
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



