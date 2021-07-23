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

package com.fraktalio.fmodel.example.statestoredsystem.adapter.web

import arrow.core.Either
import com.fraktalio.fmodel.example.statestoredsystem.adapter.persistence.RestaurantRepository
import com.fraktalio.fmodel.example.statestoredsystem.application.CommandBus
import com.fraktalio.fmodel.example.statestoredsystem.domain.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

/**
 * Rest controller - Very simple controller
 *
 * @property commandBus A command bus
 * @property restaurantRepository Restaurant repository
 * @constructor Creates Rest controller
 */
@RestController
internal class RestController(
    private val commandBus: CommandBus,
    private val restaurantRepository: RestaurantRepository
) {
    /**
     * Find all
     *
     */
    @GetMapping("/")
    fun findAll() = restaurantRepository.findAll()

    /**
     * Save new
     *
     * @param request
     */
    @PostMapping("/")
    suspend fun save(request: CreateRestaurantRequest) =
        commandBus.publish(request.convert())
}

/**
 * `Convert` extension of `CreateRestaurantRequest`
 *
 */
private fun CreateRestaurantRequest.convert() =
    Either.Right(
        CreateRestaurantCommand(
            RestaurantId(),
            this.name,
            RestaurantMenuVO(this.menuItems.map { MenuItemVO(it.id, it.name, Money(it.price)) })
        )
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