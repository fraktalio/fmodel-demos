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

package com.fraktalio.fmodel.example.eventsourcedsystem.query.adapter.eventhandler

import com.fraktalio.fmodel.application.publishTo
import com.fraktalio.fmodel.example.domain.Event
import com.fraktalio.fmodel.example.eventsourcedsystem.query.adapter.FindAllRestaurantsQuery
import com.fraktalio.fmodel.example.eventsourcedsystem.query.adapter.persistance.RestaurantCoroutineRepository
import com.fraktalio.fmodel.example.eventsourcedsystem.query.adapter.persistance.RestaurantR2DBCEntity
import com.fraktalio.fmodel.example.eventsourcedsystem.query.application.OrderRestaurantMaterializedView
import kotlinx.coroutines.flow.Flow
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
 * @property materializedView
 * @property restaurantRepository
 * @constructor Create empty Axon command handler
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@Component
@ProcessingGroup("restaurant")
internal class AxonEventHandler(
    private val materializedView: OrderRestaurantMaterializedView,
    private val restaurantRepository: RestaurantCoroutineRepository
) {

    @EventHandler
    fun handle(event: Event) {
        runBlocking {
            event.publishTo(materializedView)
        }
    }

    @QueryHandler
    fun handle(query: FindAllRestaurantsQuery): Flow<RestaurantR2DBCEntity> = restaurantRepository.findAll()
}

