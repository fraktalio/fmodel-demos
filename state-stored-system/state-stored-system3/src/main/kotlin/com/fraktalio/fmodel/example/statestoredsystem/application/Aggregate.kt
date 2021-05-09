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

import arrow.core.Either
import com.fraktalio.fmodel.application.StateRepository
import com.fraktalio.fmodel.application.StateStoredAggregate
import com.fraktalio.fmodel.domain.Decider
import com.fraktalio.fmodel.domain.extension.combineDeciders
import com.fraktalio.fmodel.example.statestoredsystem.domain.*

/**
 * One, big aggregate that is combining all deciders: [restaurantOrderDecider], [restaurantDecider].
 * It is acting as a command bus, as every command will be handled by one of the deciders.
 * The decider that is not interested in specific command type will simply ignore it (do nothing).
 *
 * @param restaurantOrderDecider restaurantOrderDecider is used internally to handle commands and produce new state.
 * @param restaurantDecider restaurantDecider is used internally to handle commands and produce new state.
 * @param aggregateRepository is used to store the newly produced state of the Restaurant and/or Restaurant order together
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
internal fun aggregate(
    restaurantOrderDecider: Decider<RestaurantOrderCommand?, RestaurantOrder?, RestaurantOrderEvent?>,
    restaurantDecider: Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>,
    aggregateRepository: StateRepository<Either<RestaurantOrderCommand?, RestaurantCommand?>, Pair<RestaurantOrder?, Restaurant?>>
) = StateStoredAggregate(

    // Combining two deciders into one.
    decider = restaurantOrderDecider.combineDeciders(restaurantDecider),
    // How and where do you want to store the new state, additionally you can do something smart with events that are emitted
    stateRepository = aggregateRepository
)



