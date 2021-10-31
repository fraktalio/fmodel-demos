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

package com.fraktalio.fmodel.example.eventsourcedsystem.query.application

import com.fraktalio.fmodel.application.MaterializedView
import com.fraktalio.fmodel.domain.combine
import com.fraktalio.fmodel.example.domain.*
import com.fraktalio.fmodel.example.eventsourcedsystem.query.adapter.persistance.MaterializedViewStateRepository

/**
 * A convenient type alias for MaterializedView<MaterializedViewState, Event?>
 */
typealias OrderRestaurantMaterializedView = MaterializedView<MaterializedViewState, Event?>

internal fun materializedView(
    restaurantViewState: RestaurantView,
    restaurantOrderViewState: RestaurantOrderView,
    viewStateRepository: MaterializedViewStateRepository
) = OrderRestaurantMaterializedView(
    view = restaurantViewState.combine(restaurantOrderViewState).dimapOnState(
        fl = { Pair(it.restaurant, it.order) },
        fr = { MaterializedViewState(it.first, it.second) }
    ),
    viewStateRepository = viewStateRepository
)

data class MaterializedViewState(val restaurant: RestaurantViewState?, val order: RestaurantOrderViewState?)
