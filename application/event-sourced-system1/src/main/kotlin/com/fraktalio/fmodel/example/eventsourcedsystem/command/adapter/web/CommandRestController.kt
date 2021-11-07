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

package com.fraktalio.fmodel.example.eventsourcedsystem.command.adapter.web

import com.fraktalio.fmodel.application.publishTo
import com.fraktalio.fmodel.example.domain.*
import com.fraktalio.fmodel.example.eventsourcedsystem.command.application.OrderRestaurantAggregate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.*

/**
 * Rest controller - Very simple controller
 *
 * @property aggregate A State Stored Aggregate - All things at Restaurant: Restaurant management, Order management
 * @property commandGateway Axon command gateway/bus
 * @constructor Creates Rest controller
 */
@RestController
internal class CommandRestController(
    private val aggregate: OrderRestaurantAggregate,
    private val commandGateway: CommandGateway
) {
    /**
     * Create restaurant
     *
     * This endpoint is sending a message/command directly to the Aggregate, so it depends on it. This is not location transparent.
     *
     * We made it a GET, but it should be POST ;)
     */
    // @PostMapping("/restaurant")
    @GetMapping("/example/restaurant/create")
    suspend fun createRestaurant() =
        CreateRestaurantRequest()
            .convertToCommand()
            .publishTo(aggregate)

    /**
     * Create restaurant via axon command bus
     *
     * This endpoint is sending a message/command via Axon Command Bus/Gateway to the Aggregate. This is location transparent!
     *
     *  We made it a GET, but it should be POST ;)
     */
    // @PostMapping("/bus/restaurant")
    @GetMapping("/example/bus/restaurant/create")
    fun createRestaurantViaBus() =
        commandGateway.send<Command>(CreateRestaurantRequest().convertToCommand())

    /**
     * Activate restaurant menu via axon command bus
     *
     * This endpoint is sending a message/command via Axon Command Bus/Gateway to the Aggregate. This is location transparent!
     *
     * We made it a GET, but it should be POST/PUT ;)
     *
     * @param restaurantId
     * @param menuId
     */
    // @PostMapping("/bus/restaurant/{restaurantId}/activate/{menuId}")
    @GetMapping("/example/bus/restaurant/{restaurantId}/activate/{menuId}")
    fun activateRestaurantMenuViaBus(@PathVariable restaurantId: UUID, @PathVariable menuId: UUID) =
        commandGateway.send<Command>(ActivateRestaurantMenuCommand(RestaurantId(restaurantId), menuId))

    /**
     * Passivate restaurant menu via axon command bus
     *
     * This endpoint is sending a message/command via Axon Command Bus/Gateway to the Aggregate. This is location transparent!
     *
     * We made it a GET, but it should be POST/PUT ;)
     *
     * @param restaurantId
     * @param menuId
     */
    // @PostMapping("/bus/restaurant/{restaurantId}/passivate/{menuId}")
    @GetMapping("/example/bus/restaurant/{restaurantId}/passivate/{menuId}")
    fun passivateRestaurantMenuViaBus(@PathVariable restaurantId: UUID, @PathVariable menuId: UUID) =
        commandGateway.send<Command>(PassivateRestaurantMenuCommand(RestaurantId(restaurantId), menuId))

    /**
     * Place restaurant order via axon command bus
     *
     * This endpoint is sending a message/command via Axon Command Bus/Gateway to the Aggregate. This is location transparent!
     *
     * We made it a GET, but it should be POST/PUT ;)
     *
     * @param restaurantId
     * @param menuId
     */
    @GetMapping("/example/bus/restaurant/{restaurantId}/order")
    fun placeRestaurantOrderViaBus(@PathVariable restaurantId: UUID) =
        commandGateway.send<Command>(
            PlaceRestaurantOrderCommand(
                RestaurantId(restaurantId),
                RestaurantOrderId(),
                persistentListOf(RestaurantOrderLineItem("1", 1, "1", "Sarma"))
            )
        )

}


/**
 * `Convert` extension of `CreateRestaurantRequest`
 *
 */
private fun CreateRestaurantRequest.convertToCommand() =
    CreateRestaurantCommand(
        RestaurantId(),
        this.name,
        RestaurantMenuVO(this.menuItems.map { MenuItemVO(it.id, it.id, it.name, Money(it.price)) }.toImmutableList())
    )


/**
 * A request for creating a Restaurant
 */
data class CreateRestaurantRequest(
    var name: String = "serbian restaurant",
    var menuItems: MutableList<MenuItemRequest> = arrayListOf(
        MenuItemRequest("1", "Sarma", BigDecimal.TEN),
        MenuItemRequest("2", "Kiselo mleko", BigDecimal.valueOf(2)),
        MenuItemRequest("3", "Srpska salata", BigDecimal.valueOf(3))
    )
)

/**
 * A Menu item request
 */
data class MenuItemRequest(var id: String, var name: String, var price: BigDecimal)
