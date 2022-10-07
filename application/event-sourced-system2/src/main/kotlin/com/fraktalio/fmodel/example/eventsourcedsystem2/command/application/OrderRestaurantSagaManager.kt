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

package com.fraktalio.fmodel.example.eventsourcedsystem2.command.application

import com.fraktalio.fmodel.application.ActionPublisher
import com.fraktalio.fmodel.application.SagaManager
import com.fraktalio.fmodel.application.sagaManager
import com.fraktalio.fmodel.domain.combine
import com.fraktalio.fmodel.example.domain.Command
import com.fraktalio.fmodel.example.domain.Event
import com.fraktalio.fmodel.example.domain.RestaurantOrderSaga
import com.fraktalio.fmodel.example.domain.RestaurantSaga
import kotlinx.coroutines.FlowPreview

/**
 * A convenient type alias for  EventSourcingAggregate<RestaurantCommand?, Restaurant?, RestaurantEvent?>
 */
typealias OrderRestaurantSagaManager = SagaManager<Event?, Command>

/**
 * @param restaurantOrderSaga restaurantOrderSaga is used internally to handle events of type RestaurantEvent and produce new commands of type RestaurantOrderCommand.
 * @param restaurantSaga restaurantSaga is used internally to handle events of type RestaurantOrderEvent and produce new commands of type RestaurantCommand.
 * @param actionPublisher is used to publish newly created commands
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@OptIn(FlowPreview::class)
internal fun sagaManager(
    restaurantOrderSaga: RestaurantOrderSaga,
    restaurantSaga: RestaurantSaga,
    actionPublisher: ActionPublisher<Command>
): OrderRestaurantSagaManager = sagaManager(
    // Combining individual choreography Sagas into one orchestrating Saga.
    saga = restaurantOrderSaga.combine(restaurantSaga),
    // How and where do you want to publish new commands.
    actionPublisher = actionPublisher
)
