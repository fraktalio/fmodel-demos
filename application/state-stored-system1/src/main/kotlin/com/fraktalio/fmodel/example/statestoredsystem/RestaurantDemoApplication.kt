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

import com.fraktalio.fmodel.application.StateRepository
import com.fraktalio.fmodel.domain.Decider
import com.fraktalio.fmodel.domain.Saga
import com.fraktalio.fmodel.example.domain.*
import com.fraktalio.fmodel.example.statestoredsystem.adapter.persistence.*
import com.fraktalio.fmodel.example.statestoredsystem.application.AggregateState
import com.fraktalio.fmodel.example.statestoredsystem.application.aggregate
import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import org.springframework.transaction.reactive.TransactionalOperator

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
    internal fun aggregateRepository(
        restaurantRepository: RestaurantCoroutineRepository,
        restaurantOrderRepository: RestaurantOrderCoroutineRepository,
        restaurantOrderItemRepository: RestaurantOrderItemCoroutineRepository,
        menuItemCoroutineRepository: MenuItemCoroutineRepository,
        operator: TransactionalOperator
    ): StateRepository<Command?, AggregateState> =
        AggregateStateStoredRepositoryImpl(
            restaurantRepository,
            restaurantOrderRepository,
            restaurantOrderItemRepository,
            menuItemCoroutineRepository,
            operator
        )

    @Bean
    internal fun aggregate(
        restaurantDecider: Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>,
        restaurantOrderDecider: Decider<RestaurantOrderCommand?, RestaurantOrder?, RestaurantOrderEvent?>,
        restaurantSaga: Saga<RestaurantOrderEvent?, RestaurantCommand?>,
        restaurantOrderSaga: Saga<RestaurantEvent?, RestaurantOrderCommand?>,
        aggregateRepository: StateRepository<Command?, AggregateState>
    ) = aggregate(restaurantOrderDecider, restaurantDecider, restaurantOrderSaga, restaurantSaga, aggregateRepository)

    @Bean
    fun initializer(connectionFactory: ConnectionFactory): ConnectionFactoryInitializer {
        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(connectionFactory)
        val populator = CompositeDatabasePopulator()
        populator.addPopulators(ResourceDatabasePopulator(ClassPathResource("./sql/schema.sql")))
        initializer.setDatabasePopulator(populator)
        return initializer
    }
}
