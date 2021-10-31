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

package com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.axon

import com.fraktalio.fmodel.application.publishTo
import com.fraktalio.fmodel.example.domain.*
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.application.RestaurantAggregate
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.application.RestaurantOrderAggregate
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.axonframework.commandhandling.CommandHandler

/**
 * External Axon command handler
 *
 * https://docs.axoniq.io/reference-guide/axon-framework/axon-framework-commands/command-handlers#external-command-handlers
 *
 * It enables `location transparency` by using Axon Server as a message/command broker.
 *
 * @property restaurantAggregate
 * @property restaurantOrderAggregate
 * @constructor Create Axon command handler
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
internal open class AxonCommandHandler(
    private val restaurantAggregate: RestaurantAggregate,
    private val restaurantOrderAggregate: RestaurantOrderAggregate
) {

    private fun publish(command: Command) {
        runBlocking {
            when (command) {
                is RestaurantCommand -> command.publishTo(restaurantAggregate).collect()
                is RestaurantOrderCommand -> command.publishTo(restaurantOrderAggregate).collect()
            }

        }
    }

    @CommandHandler
    fun handle(command: CreateRestaurantCommand) {
        publish(command)
    }

    @CommandHandler
    fun handle(command: ChangeRestaurantMenuCommand) {
        publish(command)
    }

    @CommandHandler
    fun handle(command: ActivateRestaurantMenuCommand) {
        publish(command)
    }

    @CommandHandler
    fun handle(command: PassivateRestaurantMenuCommand) {
        publish(command)
    }

    @CommandHandler
    fun handle(command: PlaceRestaurantOrderCommand) {
        publish(command)
    }

}

