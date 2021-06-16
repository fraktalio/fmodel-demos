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

import com.fraktalio.fmodel.application.ActionPublisher
import com.fraktalio.fmodel.application.SagaManager
import com.fraktalio.fmodel.domain.Saga
import com.fraktalio.fmodel.domain.combine
import com.fraktalio.fmodel.example.domain.*

/**
 * Saga manager - Orchestrator
 *
 * It is combining all the Sagas into one Saga manager that will react on Events (Action Result) of one aggregate,
 * and publish Command (Action) to other aggregates as a result.
 *
 * @param restaurantSaga Restaurant Saga
 * @param restaurantOrderSaga Restaurant Order Saga
 * @param actionPublisher Action publisher
 */
internal fun sagaManager(
    restaurantSaga: Saga<RestaurantEvent?, RestaurantOrderCommand?>,
    restaurantOrderSaga: Saga<RestaurantOrderEvent?, RestaurantCommand?>,
    actionPublisher: ActionPublisher<Command?>
) = SagaManager(
    saga = restaurantSaga.combine(restaurantOrderSaga),
    actionPublisher = actionPublisher
)
