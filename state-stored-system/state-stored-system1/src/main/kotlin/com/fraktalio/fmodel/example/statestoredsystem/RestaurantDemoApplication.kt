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

package com.fraktalio.fmodel.example.statestoredsystem

import com.fraktalio.fmodel.application.SagaManager
import com.fraktalio.fmodel.application.StateRepository
import com.fraktalio.fmodel.application.StateStoredAggregate
import com.fraktalio.fmodel.domain.Decider
import com.fraktalio.fmodel.domain.Saga
import com.fraktalio.fmodel.example.statestoredsystem.adapter.persistence.AggregateRepositoryImpl
import com.fraktalio.fmodel.example.statestoredsystem.adapter.persistence.RestaurantOrderRepository
import com.fraktalio.fmodel.example.statestoredsystem.adapter.persistence.RestaurantRepository
import com.fraktalio.fmodel.example.statestoredsystem.adapter.publisher.ActionPublisher
import com.fraktalio.fmodel.example.statestoredsystem.application.CommandBus
import com.fraktalio.fmodel.example.statestoredsystem.application.aggregate
import com.fraktalio.fmodel.example.statestoredsystem.application.sagaManager
import com.fraktalio.fmodel.example.statestoredsystem.domain.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@SpringBootApplication
class RestaurantDemoApplication

fun main(args: Array<String>) {
    runApplication<RestaurantDemoApplication>(*args)
}

/**
 * Configuration
 *
 * @constructor Creates Configuration
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@Configuration
class Configuration {

    @Bean
    internal fun restaurantDeciderBean() = restaurantDecider()

    @Bean
    internal fun restaurantOrderDeciderBean() = restaurantOrderDecider()

    @Bean
    internal fun restaurantSagaBean() = restaurantSaga()

    @Bean
    internal fun restaurantOrderSagaBean() = restaurantOrderSaga()

    @Bean
    internal fun sagaManager(
        restaurantSaga: Saga<RestaurantEvent?, RestaurantOrderCommand?>,
        restaurantOrderSaga: Saga<RestaurantOrderEvent?, RestaurantCommand?>,
        aggregate: StateStoredAggregate<Command?, Pair<RestaurantOrder?, Restaurant?>, Event?>
    ) = sagaManager(
        restaurantSaga,
        restaurantOrderSaga,
        ActionPublisher(aggregate)
    )

    @Bean
    internal fun aggregateRepository(
        restaurantRepository: RestaurantRepository,
        restaurantOrderRepository: RestaurantOrderRepository
    ): StateRepository<Command?, Pair<RestaurantOrder?, Restaurant?>> =
        AggregateRepositoryImpl(restaurantRepository, restaurantOrderRepository)

    @Bean
    internal fun aggregate(
        restaurantDecider: Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>,
        restaurantOrderDecider: Decider<RestaurantOrderCommand?, RestaurantOrder?, RestaurantOrderEvent?>,
        aggregateRepository: StateRepository<Command?, Pair<RestaurantOrder?, Restaurant?>>
    ) = aggregate(restaurantOrderDecider, restaurantDecider, aggregateRepository)

    @Bean
    internal fun commandBus(
        aggregate: StateStoredAggregate<Command?, Pair<RestaurantOrder?, Restaurant?>, Event?>,
        sagaManager: SagaManager<Event?, Command?>
    ) = CommandBus(aggregate, sagaManager)
}
