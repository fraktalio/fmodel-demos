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

package com.fraktalio.fmodel.example.eventsourcedsystem3.command.application

import com.fraktalio.fmodel.domain.Decider
import com.fraktalio.fmodel.example.domain.*
import com.fraktalio.fmodel.example.eventsourcedsystem3.command.adapter.getId
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.spring.stereotype.Aggregate


@Aggregate(commandTargetResolver = "metaDataCommandTargetResolver")
class RestaurantAxonAggregate : AbstractAggregate<RestaurantCommand?, Restaurant?, RestaurantEvent?> {

    private constructor()

    // Main decision-making component
    final override val decider: Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?> = restaurantDecider()

    // Marking one of the attributes from the Event as an aggregate identifier
    override fun RestaurantEvent?.getAggregateIdentifier(): String? = this?.getId()

    // The state of this aggregate
    override var state: Restaurant? = decider.initialState

    @CommandHandler
    constructor(command: CreateRestaurantCommand) {
        handleIt(command)
    }

    @CommandHandler
    fun handle(command: ChangeRestaurantMenuCommand) = handleIt(command)


    @CommandHandler
    fun handle(command: ActivateRestaurantMenuCommand) = handleIt(command)


    @CommandHandler
    fun handle(command: PassivateRestaurantMenuCommand) = handleIt(command)


    @CommandHandler
    fun handle(command: PlaceRestaurantOrderCommand) = handleIt(command)

}
