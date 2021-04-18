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

import arrow.core.Either
import com.fraktalio.fmodel.application.StateRepository
import com.fraktalio.fmodel.application.StateStoredAggregate
import com.fraktalio.fmodel.domain.Decider
import com.fraktalio.fmodel.domain.Saga
import com.fraktalio.fmodel.example.statestoredsystem.adapter.persistence.*
import com.fraktalio.fmodel.example.statestoredsystem.adapter.publisher.RestaurantOrderRestaurantActionPublisher
import com.fraktalio.fmodel.example.statestoredsystem.application.*
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

    // Restaurant aggregate

    @Bean
    internal fun restaurantDeciderBean() = restaurantDecider()

    @Bean("restaurantStateStoredAggregateRepository")
    internal fun restaurantStateStoredAggregateRepository(restaurantRepository: RestaurantRepository): StateRepository<RestaurantCommand?, Restaurant?> =
        RestaurantStateStoredAggregateRepositoryImpl(restaurantRepository)

    @Bean
    internal fun restaurantAggregateBean(
        restaurantDeciderBean: Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>,
        restaurantStateStoredAggregateRepository: StateRepository<RestaurantCommand?, Restaurant?>,
    ) = restaurantAggregate(restaurantDeciderBean, restaurantStateStoredAggregateRepository)

    // Order aggregate

    @Bean
    internal fun restaurantOrderDeciderBean() = restaurantOrderDecider()

    @Bean("restaurantOrderStateStoredAggregateRepository")
    internal fun restaurantOrderStateStoredAggregateRepository(restaurantOrderRepository: RestaurantOrderRepository): StateRepository<RestaurantOrderCommand?, RestaurantOrder?> =
        RestaurantOrderStateStoredAggregateRepositoryImpl(restaurantOrderRepository)

    @Bean
    internal fun restaurantOrderAggregateBean(
        restaurantOrderDeciderBean: Decider<RestaurantOrderCommand?, RestaurantOrder?, RestaurantOrderEvent?>,
        restaurantOrderStateStoredAggregateRepository: StateRepository<RestaurantOrderCommand?, RestaurantOrder?>
    ) = restaurantOrderAggregate(restaurantOrderDeciderBean, restaurantOrderStateStoredAggregateRepository)

    // Restaurant - Order Saga

    @Bean
    internal fun restaurantOrderRestaurantSagaBean() = restaurantOrderRestaurantSaga()

    @Bean
    internal fun restaurantOrderRestaurantSagaManagerBean(
        restaurantOrderRestaurantSagaBean: Saga<RestaurantOrderRestaurantActionResult?, RestaurantOrderRestaurantAction?>,
        restaurantOrderAggregateBean: StateStoredAggregate<RestaurantOrderCommand?, RestaurantOrder?, RestaurantOrderEvent?>
    ) = restaurantOrderRestaurantSagaManager(
        restaurantOrderRestaurantSagaBean,
        RestaurantOrderRestaurantActionPublisher(restaurantOrderAggregateBean)
    )

}
