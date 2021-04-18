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

package com.fraktalio.fmodel.example.statestoredsystem.application

import com.fraktalio.fmodel.application.StateRepository
import com.fraktalio.fmodel.application.StateStoredAggregate
import com.fraktalio.fmodel.domain.Decider
import com.fraktalio.fmodel.example.statestoredsystem.domain.RestaurantOrder
import com.fraktalio.fmodel.example.statestoredsystem.domain.RestaurantOrderCommand
import com.fraktalio.fmodel.example.statestoredsystem.domain.RestaurantOrderEvent

/**
 * State stored aggregate is using/delegating a `restaurantOrderDecider` from the domain layer to handle commands and produce new state.
 * In order to handle the command, aggregate needs to fetch the current state via `aggregateStateRepository.fetchState` function first, and then delegate the command to the decider which can produce new state as a result.
 * New state is then stored via `aggregateStateRepository.save` suspending function.
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 *
 * @param restaurantOrderDecider restaurantOrderDecider is used internally to handle commands and produce new state.
 * @param restaurantOrderStateStoredAggregateRepository is used to store the newly produced state.
 */
internal fun restaurantOrderAggregate(
    restaurantOrderDecider: Decider<RestaurantOrderCommand?, RestaurantOrder?, RestaurantOrderEvent?>,
    restaurantOrderStateStoredAggregateRepository: StateRepository<RestaurantOrderCommand?, RestaurantOrder?>
) = StateStoredAggregate(
    // Decision making algorithm
    decider = restaurantOrderDecider,
    // How and where do you want to store the new state of the aggregate
    stateRepository = restaurantOrderStateStoredAggregateRepository
)

