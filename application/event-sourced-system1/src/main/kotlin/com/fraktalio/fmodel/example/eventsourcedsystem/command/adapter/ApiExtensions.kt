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

package com.fraktalio.fmodel.example.eventsourcedsystem.command.adapter

import com.fraktalio.fmodel.example.domain.Command
import com.fraktalio.fmodel.example.domain.Event
import com.fraktalio.fmodel.example.domain.RestaurantEvent
import com.fraktalio.fmodel.example.domain.RestaurantOrderEvent
import org.axonframework.commandhandling.RoutingKey

//Test this! ;)
@RoutingKey
fun Command.getId() = identifierString


fun Event.getId() = identifierString

fun Event.getAggregateType() = when (this) {
    is RestaurantEvent -> "Restaurant"
    is RestaurantOrderEvent -> "RestaurantOrder"
}
