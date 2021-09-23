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
import com.fraktalio.fmodel.example.domain.Restaurant.*
import com.fraktalio.fmodel.example.domain.Restaurant.MenuStatus.ACTIVE
import com.fraktalio.fmodel.example.domain.Restaurant.MenuStatus.PASSIVE
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import java.util.*
import java.util.stream.Collectors

/**
 * Decider is a pure domain component.
 * Decider is a datatype that represents the main decision-making algorithm.
 *
 * `decide / command handlers` is a pure function/lambda that takes any command of type [RestaurantCommand] and input state of type [Restaurant] as parameters, and returns the flow of output events Flow<[RestaurantEvent]> as a result
 * `evolve / event-sourcing handlers` is a pure function/lambda that takes input state of type [Restaurant] and input event of type [RestaurantEvent] as parameters, and returns the output/new state [Restaurant]
 * `initialState` is a starting point / An initial state of [Restaurant]. In our case, it is `null`
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun restaurantDecider() = Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>(
    initialState = null,
    // Command handling part: for each type of [RestaurantCommand] you are going to publish specific events/facts, as required.
    decide = { c, s ->
        when {
            (c is CreateRestaurantCommand) && (s == null) -> flowOf(
                RestaurantCreatedEvent(
                    c.identifier,
                    c.name,
                    c.menu
                )
            )
            (c is ChangeRestaurantMenuCommand) && (s != null) -> flowOf(
                RestaurantMenuChangedEvent(
                    c.identifier,
                    c.menu
                )
            )
            (c is ActivateRestaurantMenuCommand) && (s != null) -> flowOf(
                RestaurantMenuActivatedEvent(
                    c.identifier,
                    c.menuId
                )
            )
            (c is PassivateRestaurantMenuCommand) && (s != null) -> flowOf(
                RestaurantMenuPassivatedEvent(
                    c.identifier,
                    c.menuId
                )
            )
            (c is PlaceRestaurantOrderCommand) && (s != null) && s.isValid(c) -> flowOf(
                RestaurantOrderPlacedAtRestaurantEvent(
                    c.identifier,
                    c.lineItems,
                    c.restaurantOrderIdentifier
                )
            )
            (c is PlaceRestaurantOrderCommand) && (s != null) && !s.isValid(c) -> flowOf(
                RestaurantOrderRejectedByRestaurantEvent(
                    c.identifier,
                    c.restaurantOrderIdentifier,
                    "Not on the menu"
                )
            )
            else -> emptyFlow()
        }
    },
    // Event-sourcing handling part: for each event of type [RestaurantEvent] you are going to evolve to a new state of the [Restaurant]
    evolve = { s, e ->
        when {
            (e is RestaurantCreatedEvent) -> Restaurant(
                e.identifier,
                e.name,
                RestaurantMenu(
                    e.menu.menuId,
                    e.menu.menuItems.map { MenuItem(it.id, it.menuItemId, it.name, it.price) },
                    e.menu.cuisine
                ),
                Status.OPEN
            )
            (e is RestaurantMenuChangedEvent) && (s != null) -> Restaurant(
                s.id,
                s.name,
                RestaurantMenu(
                    e.menu.menuId,
                    e.menu.menuItems.map { MenuItem(it.id, it.menuItemId, it.name, it.price) },
                    e.menu.cuisine
                ), s.status
            )
            (e is RestaurantMenuActivatedEvent) && (s != null) -> Restaurant(
                s.id,
                s.name,
                RestaurantMenu(
                    s.menu.menuId,
                    s.menu.items,
                    s.menu.cuisine,
                    ACTIVE
                ), s.status
            )
            (e is RestaurantMenuPassivatedEvent) && (s != null) -> Restaurant(
                s.id,
                s.name,
                RestaurantMenu(
                    s.menu.menuId,
                    s.menu.items,
                    s.menu.cuisine,
                    PASSIVE
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



