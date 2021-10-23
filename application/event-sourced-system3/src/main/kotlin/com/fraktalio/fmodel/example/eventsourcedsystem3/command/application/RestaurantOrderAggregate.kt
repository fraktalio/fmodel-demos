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

import com.fraktalio.fmodel.example.domain.*
import com.fraktalio.fmodel.example.eventsourcedsystem3.command.adapter.getId
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.spring.stereotype.Aggregate

@Aggregate(commandTargetResolver = "metaDataCommandTargetResolver")
class RestaurantOrderAxonAggregate :
    AbstractAggregate<RestaurantOrderCommand?, RestaurantOrder?, RestaurantOrderEvent?> {

    private constructor()

    // Main decision-making component
    final override val decider = restaurantOrderDecider()

    // Marking one of the attributes from the Event as an aggregate identifier
    override fun RestaurantOrderEvent?.getAggregateIdentifier(): String? = this?.getId()

    // The state of this aggregate
    override var state: RestaurantOrder? = decider.initialState

    @CommandHandler
    constructor(command: CreateRestaurantOrderCommand) {
        handleIt(command)
    }

    @CommandHandler
    fun handle(command: MarkRestaurantOrderAsPreparedCommand) = handleIt(command)
}
