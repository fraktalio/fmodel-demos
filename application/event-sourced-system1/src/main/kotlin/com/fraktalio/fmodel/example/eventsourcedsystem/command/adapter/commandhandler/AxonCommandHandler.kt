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

package com.fraktalio.fmodel.example.eventsourcedsystem.command.adapter.commandhandler

import com.fraktalio.fmodel.application.publishTo
import com.fraktalio.fmodel.example.domain.*
import com.fraktalio.fmodel.example.eventsourcedsystem.command.application.OrderRestaurantAggregate
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
 * @property aggregate
 * @constructor Create empty Axon command handler
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
internal open class AxonCommandHandler(private val aggregate: OrderRestaurantAggregate) {

    private fun publish(command: Command) {
        runBlocking {
            command.publishTo(aggregate).collect()
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

    @CommandHandler
    fun handle(command: CreateRestaurantOrderCommand) {
        publish(command)
    }

}

