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
import com.fraktalio.fmodel.application.EventSourcingAggregate
import com.fraktalio.fmodel.domain.Decider
import com.fraktalio.fmodel.domain.Saga
import com.fraktalio.fmodel.example.domain.*
import com.fraktalio.fmodel.example.eventsourcedsystem.adapter.commandhandler.AxonCommandHandler
import com.fraktalio.fmodel.example.eventsourcedsystem.adapter.persistence.AggregateEventStoreRepositoryImpl
import com.fraktalio.fmodel.example.eventsourcedsystem.application.aggregate
import org.axonframework.eventsourcing.eventstore.EventStore
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
    internal fun aggregateRepository(axonServerEventStore: EventStore): EventRepository<Command?, Event?> =
        AggregateEventStoreRepositoryImpl(axonServerEventStore)

    @Bean
    internal fun aggregate(
        restaurantDecider: Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>,
        restaurantOrderDecider: Decider<RestaurantOrderCommand?, RestaurantOrder?, RestaurantOrderEvent?>,
        restaurantSaga: Saga<RestaurantOrderEvent?, RestaurantCommand?>,
        restaurantOrderSaga: Saga<RestaurantEvent?, RestaurantOrderCommand?>,
        eventRepository: EventRepository<Command?, Event?>
    ) = aggregate(restaurantOrderDecider, restaurantDecider, restaurantOrderSaga, restaurantSaga, eventRepository)


    @Bean
    internal fun commandHandler(aggregate: EventSourcingAggregate<Command?, Pair<RestaurantOrder?, Restaurant?>, Event?>) =
        AxonCommandHandler(aggregate)
}

