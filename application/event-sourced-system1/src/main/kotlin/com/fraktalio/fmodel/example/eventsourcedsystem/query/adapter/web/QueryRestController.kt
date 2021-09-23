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

package com.fraktalio.fmodel.example.eventsourcedsystem.query.adapter.web

import com.fraktalio.fmodel.example.eventsourcedsystem.query.adapter.FindAllRestaurantsQuery
import com.fraktalio.fmodel.example.eventsourcedsystem.query.adapter.persistance.RestaurantCoroutineRepository
import com.fraktalio.fmodel.example.eventsourcedsystem.query.adapter.persistance.RestaurantR2DBCEntity
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Query side Rest controller - Very simple controller
 *
 * @property restaurantRepository
 * @property queryGateway Axon query gateway/bus
 * @constructor Creates Rest controller
 */
@RestController
internal class QueryRestController(
    private val restaurantRepository: RestaurantCoroutineRepository,
    private val queryGateway: QueryGateway
) {
    /**
     * Get restaurants - non location transparent version of the endpoint
     **/
    @GetMapping("/example/restaurant")
    suspend fun getRestaurants() =
        restaurantRepository.findAll()

    /**
     * Get restaurants - location transparent version of the endpoint.
     * Using the Axon Query Bus/Gateway
     **/
    @GetMapping("/example/bus/restaurant")
    suspend fun getRestaurantsViaQueryBus() = queryGateway.query(
        FindAllRestaurantsQuery(),
        ResponseTypes.multipleInstancesOf(RestaurantR2DBCEntity::class.java)
    )
}
