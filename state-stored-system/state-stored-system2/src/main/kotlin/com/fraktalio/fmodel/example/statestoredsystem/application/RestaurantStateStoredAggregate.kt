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
import com.fraktalio.fmodel.example.statestoredsystem.domain.Restaurant
import com.fraktalio.fmodel.example.statestoredsystem.domain.RestaurantCommand
import com.fraktalio.fmodel.example.statestoredsystem.domain.RestaurantEvent

/**
 * State stored aggregate is using/delegating a `restaurantDecider` to handle commands and produce new state.
 * In order to handle the command, aggregate needs to fetch the current state via `aggregateStateRepository.fetchState` function first, and then delegate the command to the decider which can produce new state as a result.
 * New state is then stored via `aggregateStateRepository.save` suspending function.
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 *
 * @param restaurantDecider restaurantDecider is used internally to handle commands and produce new state.
 * @param restaurantStateStoredAggregateRepository is used to store the newly produced state.
 */
internal fun restaurantAggregate(
    restaurantDecider: Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>,
    restaurantStateStoredAggregateRepository: StateRepository<RestaurantCommand?, Restaurant?>
) = StateStoredAggregate(
    // Decision making algorithm
    decider = restaurantDecider,
    // How and where do you want to store the new state of the aggregate, and eventually forward the actions further.
    stateRepository = restaurantStateStoredAggregateRepository
//    storeState = { s, e ->
//        catch {
//            // Saving the new state
//            if (s != null) restaurantStateStoredAggregateRepository.save(s)
//            // Mapping Saga manager to be able to receive event as an action result type
//            val sagaManager = restaurantOrderRestaurantSagaManager.lmapOnAR { aRn: RestaurantEvent ->
//                when (aRn) {
//                    is RestaurantOrderPlacedAtRestaurantEvent -> RestaurantOrderPlacedAtRestaurantActionResult(
//                        aRn.identifier,
//                        aRn.restaurantOrderId,
//                        aRn.lineItems
//                    )
//                    else -> null
//                }
//            }
//            // Publishing action results (events in this case) explicitly to a Saga manager
//            e.filterNotNull().forEach {
//                if (sagaManager.handle(it).isLeft()) throw RuntimeException("unable to handle action result $it")
//            }
//            Success.StateStoredAndEventsPublishedSuccessfully(s, e)
//        }.mapLeft { throwable -> Error.StoringStateFailed(s, throwable) }
//
//    },
//    // How and from where do you want to read the old state of the aggregate
//    fetchState = { c ->
//        catch {
//            if (c != null) restaurantStateStoredAggregateRepository.findById(c.identifier) else null
//        }.mapLeft { throwable -> Error.FetchingStateFailed(throwable) }
//
//    }
)

