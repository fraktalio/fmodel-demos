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
import com.fraktalio.fmodel.application.EventSourcingAggregate
import com.fraktalio.fmodel.application.ViewStateRepository
import com.fraktalio.fmodel.domain.Decider
import com.fraktalio.fmodel.domain.Saga
import com.fraktalio.fmodel.domain.View
import com.fraktalio.fmodel.example.domain.*
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.axon.ActionPublisherImpl
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.axon.AxonCommandHandler
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.getId
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.persistence.RestaurantAggregateEventStoreRepositoryImpl
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.persistence.RestaurantOrderAggregateEventStoreRepositoryImpl
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.application.restaurantAggregate
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.application.restaurantOrderAggregate
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.application.sagaManager
import com.fraktalio.fmodel.example.eventsourcedsystem2.query.adapter.persistance.*
import com.fraktalio.fmodel.example.eventsourcedsystem2.query.application.restaurantMaterializedView
import com.fraktalio.fmodel.example.eventsourcedsystem2.query.application.restaurantOrderMaterializedView
import io.r2dbc.spi.ConnectionFactory
import org.axonframework.commandhandling.distributed.MetaDataRoutingStrategy
import org.axonframework.commandhandling.distributed.RoutingStrategy
import org.axonframework.commandhandling.distributed.UnresolvedRoutingKeyPolicy
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventsourcing.eventstore.EventStore
import org.axonframework.messaging.Message
import org.axonframework.messaging.MessageDispatchInterceptor
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
        restaurantDecider: Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>,
        restaurantAggregateRepositoryBean: EventRepository<RestaurantCommand?, RestaurantEvent?>
    ) = restaurantAggregate(restaurantDecider, restaurantAggregateRepositoryBean)

    @Bean
    internal fun restaurantOrderAggregateBean(
        restaurantOrderDecider: Decider<RestaurantOrderCommand?, RestaurantOrder?, RestaurantOrderEvent?>,
        restaurantOrderAggregateRepositoryBean: EventRepository<RestaurantOrderCommand?, RestaurantOrderEvent?>
    ) = restaurantOrderAggregate(restaurantOrderDecider, restaurantOrderAggregateRepositoryBean)

    @Bean
    internal fun actionPublisherBean(commandGateway: CommandGateway): ActionPublisher<Command?> =
        ActionPublisherImpl(commandGateway)

    @Bean
    internal fun orderRestaurantSagaManagerBean(
        restaurantSaga: Saga<RestaurantOrderEvent?, RestaurantCommand?>,
        restaurantOrderSaga: Saga<RestaurantEvent?, RestaurantOrderCommand?>,
        actionPublisher: ActionPublisher<Command?>
    ) = sagaManager(restaurantOrderSaga, restaurantSaga, actionPublisher)

    @Bean
    internal fun commandHandlerBean(
        restaurantAggregate: EventSourcingAggregate<RestaurantCommand?, Restaurant?, RestaurantEvent?>,
        restaurantOrderAggregate: EventSourcingAggregate<RestaurantOrderCommand?, RestaurantOrder?, RestaurantOrderEvent?>
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
    ): ViewStateRepository<RestaurantEvent?, RestaurantView?> =
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
    ): ViewStateRepository<RestaurantOrderEvent?, RestaurantOrderView?> =
        RestaurantOrderMaterializedViewStateRepositoryImpl(
            restaurantOrderRepository,
            restaurantOrderItemRepository,
            operator
        )

    @Bean
    internal fun restaurantMaterializedViewBean(
        restaurantView: View<RestaurantView?, RestaurantEvent?>,
        viewStateRepository: ViewStateRepository<RestaurantEvent?, RestaurantView?>
    ) = restaurantMaterializedView(restaurantView, viewStateRepository)

    @Bean
    internal fun restaurantOrderMaterializedViewBean(
        restaurantOrderView: View<RestaurantOrderView?, RestaurantOrderEvent?>,
        viewStateRepository: ViewStateRepository<RestaurantOrderEvent?, RestaurantOrderView?>
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
    fun registerCommandBusInterceptors(commandGateway: CommandGateway) {
        commandGateway.registerDispatchInterceptor(RoutingInterceptor())
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
