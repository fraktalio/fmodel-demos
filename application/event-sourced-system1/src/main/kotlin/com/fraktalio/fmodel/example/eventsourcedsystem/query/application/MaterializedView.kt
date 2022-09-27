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
import com.fraktalio.fmodel.application.materializedView
import com.fraktalio.fmodel.domain.combine
import com.fraktalio.fmodel.example.domain.*
import com.fraktalio.fmodel.example.eventsourcedsystem.query.adapter.persistance.MaterializedViewStateRepository

/**
 * A convenient type alias for MaterializedView<MaterializedViewState, Event?>
 */
typealias OrderRestaurantMaterializedView = MaterializedView<MaterializedViewState, Event?>

/**
 * One, big materialized view that is `combining` all views: [restaurantView], [restaurantOrderView].
 * Every event will be handled by one of the views.
 * The view that is not interested in specific event type will simply ignore it (do nothing).
 *
 * @param restaurantView restaurantView is used internally to handle events and maintain a view state.
 * @param restaurantOrderView restaurantOrderView is used internally to handle events and maintain a view state.
 * @param viewStateRepository is used to store the newly produced view state of the Restaurant and/or Restaurant order together
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
internal fun materializedView(
    restaurantView: RestaurantView,
    restaurantOrderView: RestaurantOrderView,
    viewStateRepository: MaterializedViewStateRepository
): OrderRestaurantMaterializedView = materializedView(
    // Combining two views into one, and (di)map the inconvenient Pair into a domain specific Data class (MaterializedViewState) that will represent view state better.
    view = restaurantView.combine(restaurantOrderView).dimapOnState(
        fl = { Pair(it.restaurant, it.order) },
        fr = { MaterializedViewState(it.first, it.second) }
    ),
    // How and where do you want to store new view state.
    viewStateRepository = viewStateRepository
)

/**
 * A domain specific representation of the combined view state.
 */
data class MaterializedViewState(val restaurant: RestaurantViewState?, val order: RestaurantOrderViewState?)
