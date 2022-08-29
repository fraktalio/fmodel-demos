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

package com.fraktalio.fmodel.example.domain

import com.fraktalio.fmodel.domain.Saga
import kotlinx.coroutines.flow.emptyFlow

/**
 * A convenient type alias for Saga<RestaurantEvent?, RestaurantOrderCommand>
 */
typealias RestaurantSaga = Saga<RestaurantOrderEvent?, RestaurantCommand>

/**
 * Saga is a datatype that represents the central point of control deciding what to execute next.
 * It is responsible for mapping different events from aggregates into action results (AR) that the [Saga] then can use to calculate the next actions (A) to be mapped to command of other aggregates.
 *
 * Saga does not maintain the state.
 *
 * `react` is a pure function/lambda that takes any event/action-result of type [RestaurantOrderEvent] as parameter, and returns the flow of commands/actions Flow<[RestaurantCommand]> to be published further downstream.
 */
fun restaurantSaga() = RestaurantSaga(
    react = { e ->
        when (e) {
            //TODO evolve the example ;), it does not do much at the moment.
            is RestaurantOrderCreatedEvent -> emptyFlow()
            is RestaurantOrderPreparedEvent -> emptyFlow()
            is RestaurantOrderErrorEvent -> emptyFlow()
            null -> emptyFlow() // We ignore the `null` event by returning the empty flow of commands. Only the Saga that can handle `null` event/action-result can be combined (Monoid) with other Sagas.
        }
    }
)
