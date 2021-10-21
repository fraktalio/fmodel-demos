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

package com.fraktalio.fmodel.example.eventsourcedsystem2.query.adapter.axon

import com.fraktalio.fmodel.application.MaterializedView
import com.fraktalio.fmodel.application.publishTo
import com.fraktalio.fmodel.example.domain.RestaurantEvent
import com.fraktalio.fmodel.example.domain.RestaurantOrderEvent
import com.fraktalio.fmodel.example.domain.RestaurantOrderView
import com.fraktalio.fmodel.example.domain.RestaurantView
import com.fraktalio.fmodel.example.eventsourcedsystem2.query.adapter.FindAllRestaurantsQuery
import com.fraktalio.fmodel.example.eventsourcedsystem2.query.adapter.persistance.RestaurantCoroutineRepository
import kotlinx.coroutines.runBlocking
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component

/**
 * Axon event handler
 *
 * It enables `location transparency` by using Axon Server as a message/event broker.
 *
 * @property rMaterializedView
 * @property roMaterializedView
 * @property restaurantRepository
 * @constructor Create empty Axon command handler
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@Component
@ProcessingGroup("restaurant")
internal class AxonEventHandler(
    private val rMaterializedView: MaterializedView<RestaurantView?, RestaurantEvent?>,
    private val roMaterializedView: MaterializedView<RestaurantOrderView?, RestaurantOrderEvent?>,
    private val restaurantRepository: RestaurantCoroutineRepository
) {

    @EventHandler
    fun handle(event: RestaurantEvent) {
        runBlocking {
            event.publishTo(rMaterializedView)
        }
    }

    @EventHandler
    fun handle(event: RestaurantOrderEvent) {
        runBlocking {
            event.publishTo(roMaterializedView)
        }
    }

    @QueryHandler
    fun handle(query: FindAllRestaurantsQuery) = restaurantRepository.findAll()
}

