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

package com.fraktalio.fmodel.example.eventsourcedsystem3

import com.fraktalio.fmodel.example.domain.*
import com.fraktalio.fmodel.example.eventsourcedsystem3.command.adapter.getId
import io.r2dbc.spi.ConnectionFactory
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.commandhandling.distributed.MetaDataRoutingStrategy
import org.axonframework.commandhandling.distributed.RoutingStrategy
import org.axonframework.commandhandling.distributed.UnresolvedRoutingKeyPolicy
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.Message
import org.axonframework.messaging.MessageDispatchInterceptor
import org.axonframework.modelling.command.CommandTargetResolver
import org.axonframework.modelling.command.VersionedAggregateIdentifier
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
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


    // VIEW - QUERY

    @Bean
    internal fun restaurantViewBean() = restaurantView()

    @Bean
    internal fun restaurantOrderViewBean() = restaurantOrderView()

    // R2DBC initializer

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

    @Bean
    fun metaDataCommandTargetResolver(): MetaDataCommandTargetResolver =
        MetaDataCommandTargetResolver()

    @Autowired
    fun registerCommandBusInterceptors(commandGateway: CommandGateway) {
        commandGateway.registerDispatchInterceptor(RoutingInterceptor())
    }
}

@Suppress("UNCHECKED_CAST")
class RoutingInterceptor<T : Message<*>> : MessageDispatchInterceptor<T> {


    override fun handle(messages: List<T>): BiFunction<Int, T, T> {
        return BiFunction { _: Int?, message: T ->
            val m = message.andMetaData(mapOf(Pair("aggregateIdentifier", (message.payload as Command).getId())))
            return@BiFunction m as T
        }
    }
}

class MetaDataCommandTargetResolver : CommandTargetResolver {
    override fun resolveTarget(command: CommandMessage<*>): VersionedAggregateIdentifier {
        val aggregateIdentifier: String = command.metaData["aggregateIdentifier"].toString()
        val aggregateVersion: Long? = command.metaData["aggregateVersion"]?.toString()?.toLong()

        return VersionedAggregateIdentifier(aggregateIdentifier, aggregateVersion)
    }

}
