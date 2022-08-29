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

package com.fraktalio.fmodel.example.eventsourcedsystem2

import com.fraktalio.fmodel.application.ActionPublisher
import com.fraktalio.fmodel.application.EventRepository
import com.fraktalio.fmodel.example.domain.*
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.bus.ActionPublisherImpl
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.bus.AxonCommandHandler
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.getId
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.persistence.RestaurantAggregateEventStoreRepositoryImpl
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.persistence.RestaurantOrderAggregateEventStoreRepositoryImpl
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.application.*
import com.fraktalio.fmodel.example.eventsourcedsystem2.query.adapter.persistance.*
import com.fraktalio.fmodel.example.eventsourcedsystem2.query.application.restaurantMaterializedView
import com.fraktalio.fmodel.example.eventsourcedsystem2.query.application.restaurantOrderMaterializedView
import io.r2dbc.spi.ConnectionFactory
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.distributed.MetaDataRoutingStrategy
import org.axonframework.commandhandling.distributed.RoutingStrategy
import org.axonframework.commandhandling.distributed.UnresolvedRoutingKeyPolicy
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventsourcing.eventstore.EventStore
import org.axonframework.messaging.Message
import org.axonframework.messaging.MessageDispatchInterceptor
import org.axonframework.messaging.interceptors.LoggingInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import org.springframework.transaction.reactive.TransactionalOperator
import java.util.function.BiFunction

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
    internal fun restaurantAggregateRepositoryBean(axonServerEventStore: EventStore): EventRepository<RestaurantCommand?, RestaurantEvent?> =
        RestaurantAggregateEventStoreRepositoryImpl(axonServerEventStore)

    @Bean
    internal fun restaurantOrderAggregateRepositoryBean(axonServerEventStore: EventStore): EventRepository<RestaurantOrderCommand?, RestaurantOrderEvent?> =
        RestaurantOrderAggregateEventStoreRepositoryImpl(axonServerEventStore)

    @Bean
    internal fun restaurantAggregateBean(
        restaurantDecider: RestaurantDecider,
        restaurantAggregateRepositoryBean: EventRepository<RestaurantCommand?, RestaurantEvent?>
    ) = restaurantAggregate(restaurantDecider, restaurantAggregateRepositoryBean)

    @Bean
    internal fun restaurantOrderAggregateBean(
        restaurantOrderDecider: RestaurantOrderDecider,
        restaurantOrderAggregateRepositoryBean: EventRepository<RestaurantOrderCommand?, RestaurantOrderEvent?>
    ) = restaurantOrderAggregate(restaurantOrderDecider, restaurantOrderAggregateRepositoryBean)

    @Bean
    internal fun actionPublisherBean(commandGateway: CommandGateway): ActionPublisher<Command> =
        ActionPublisherImpl(commandGateway)

    @Bean
    internal fun orderRestaurantSagaManagerBean(
        restaurantSaga: RestaurantSaga,
        restaurantOrderSaga: RestaurantOrderSaga,
        actionPublisher: ActionPublisher<Command>
    ) = sagaManager(restaurantOrderSaga, restaurantSaga, actionPublisher)

    @Bean
    internal fun commandHandlerBean(
        restaurantAggregate: RestaurantAggregate,
        restaurantOrderAggregate: RestaurantOrderAggregate
    ) = AxonCommandHandler(restaurantAggregate, restaurantOrderAggregate)

    // VIEW - QUERY

    @Bean
    internal fun restaurantViewBean() = restaurantView()

    @Bean
    internal fun restaurantOrderViewBean() = restaurantOrderView()

    @Bean
    internal fun restaurantViewStateRepositoryBean(
        restaurantRepository: RestaurantCoroutineRepository,
        menuItemCoroutineRepository: MenuItemCoroutineRepository,
        operator: TransactionalOperator
    ): RestaurantMaterializedViewStateRepository =
        RestaurantMaterializedViewStateRepositoryImpl(
            restaurantRepository,
            menuItemCoroutineRepository,
            operator
        )

    @Bean
    internal fun restaurantOrderViewStateRepositoryBean(
        restaurantOrderRepository: RestaurantOrderCoroutineRepository,
        restaurantOrderItemRepository: RestaurantOrderItemCoroutineRepository,
        operator: TransactionalOperator
    ): RestaurantOrderMaterializedViewStateRepository =
        RestaurantOrderMaterializedViewStateRepositoryImpl(
            restaurantOrderRepository,
            restaurantOrderItemRepository,
            operator
        )

    @Bean
    internal fun restaurantMaterializedViewBean(
        restaurantView: RestaurantView,
        viewStateRepository: RestaurantMaterializedViewStateRepository
    ) = restaurantMaterializedView(restaurantView, viewStateRepository)

    @Bean
    internal fun restaurantOrderMaterializedViewBean(
        restaurantOrderView: RestaurantOrderView,
        viewStateRepository: RestaurantOrderMaterializedViewStateRepository
    ) = restaurantOrderMaterializedView(restaurantOrderView, viewStateRepository)

    @Bean
    fun initializer(connectionFactory: ConnectionFactory): ConnectionFactoryInitializer {
        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(connectionFactory)
        val populator = CompositeDatabasePopulator()
        populator.addPopulators(ResourceDatabasePopulator(ClassPathResource("./sql/schema.sql")))
        initializer.setDatabasePopulator(populator)
        return initializer
    }

    // Axon

    @Bean
    fun routingStrategy(): RoutingStrategy =
        MetaDataRoutingStrategy.builder()
            .metaDataKey("aggregateIdentifier")
            .fallbackRoutingStrategy(UnresolvedRoutingKeyPolicy.RANDOM_KEY)
            .build()

    @Autowired
    fun registerCommandGatewayInterceptors(commandGateway: CommandGateway) {
        commandGateway.registerDispatchInterceptor(RoutingInterceptor())
        commandGateway.registerDispatchInterceptor(LoggingInterceptor())
    }

    @Autowired
    fun registerCommandGatewayInterceptors(commandBus: CommandBus) {
        commandBus.registerHandlerInterceptor(LoggingInterceptor())
    }
}


class RoutingInterceptor<T : Message<*>> : MessageDispatchInterceptor<T> {
    override fun handle(messages: List<T>): BiFunction<Int, T, T> {
        return BiFunction { _: Int?, message: T ->
            val m = message.andMetaData(mapOf(Pair("aggregateIdentifier", (message.payload as Command).getId())))
            m as T
        }
    }

}
