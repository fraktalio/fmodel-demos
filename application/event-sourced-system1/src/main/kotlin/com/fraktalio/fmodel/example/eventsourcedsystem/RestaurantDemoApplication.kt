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

package com.fraktalio.fmodel.example.eventsourcedsystem

import com.fraktalio.fmodel.application.EventRepository
import com.fraktalio.fmodel.example.domain.*
import com.fraktalio.fmodel.example.eventsourcedsystem.command.adapter.commandhandler.AxonCommandHandler
import com.fraktalio.fmodel.example.eventsourcedsystem.command.adapter.persistence.AggregateEventStoreRepository
import com.fraktalio.fmodel.example.eventsourcedsystem.command.adapter.persistence.AggregateEventStoreRepositoryImpl
import com.fraktalio.fmodel.example.eventsourcedsystem.command.application.OrderRestaurantAggregate
import com.fraktalio.fmodel.example.eventsourcedsystem.command.application.aggregate
import com.fraktalio.fmodel.example.eventsourcedsystem.query.adapter.persistance.*
import com.fraktalio.fmodel.example.eventsourcedsystem.query.application.materializedView
import io.r2dbc.spi.ConnectionFactory
import org.axonframework.eventsourcing.eventstore.EventStore
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

    // COMMAND

    @Bean
    internal fun restaurantDeciderBean() = restaurantDecider()

    @Bean
    internal fun restaurantOrderDeciderBean() = restaurantOrderDecider()

    @Bean
    internal fun restaurantSagaBean() = restaurantSaga()

    @Bean
    internal fun restaurantOrderSagaBean() = restaurantOrderSaga()

    @Bean
    internal fun aggregateRepositoryBean(axonServerEventStore: EventStore): EventRepository<Command?, Event?> =
        AggregateEventStoreRepositoryImpl(axonServerEventStore)

    @Bean
    internal fun aggregateBean(
        restaurantDecider: RestaurantDecider,
        restaurantOrderDecider: RestaurantOrderDecider,
        restaurantSaga: RestaurantSaga,
        restaurantOrderSaga: RestaurantOrderSaga,
        eventRepository: AggregateEventStoreRepository
    ) = aggregate(restaurantOrderDecider, restaurantDecider, restaurantOrderSaga, restaurantSaga, eventRepository)

    @Bean
    internal fun commandHandlerBean(aggregate: OrderRestaurantAggregate) =
        AxonCommandHandler(aggregate)

    // VIEW - QUERY

    @Bean
    internal fun restaurantViewBean() = restaurantView()

    @Bean
    internal fun restaurantOrderViewBean() = restaurantOrderView()

    @Bean
    internal fun viewStateRepositoryBean(
        restaurantRepository: RestaurantCoroutineRepository,
        restaurantOrderRepository: RestaurantOrderCoroutineRepository,
        restaurantOrderItemRepository: RestaurantOrderItemCoroutineRepository,
        menuItemCoroutineRepository: MenuItemCoroutineRepository,
        operator: TransactionalOperator
    ): MaterializedViewStateRepository =
        MaterializedViewStateRepositoryImpl(
            restaurantRepository,
            restaurantOrderRepository,
            restaurantOrderItemRepository,
            menuItemCoroutineRepository,
            operator
        )

    @Bean
    internal fun materializedViewBean(
        restaurantView: RestaurantView,
        restaurantOrderViewState: RestaurantOrderView,
        viewStateRepository: MaterializedViewStateRepository
    ) = materializedView(restaurantView, restaurantOrderViewState, viewStateRepository)

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

