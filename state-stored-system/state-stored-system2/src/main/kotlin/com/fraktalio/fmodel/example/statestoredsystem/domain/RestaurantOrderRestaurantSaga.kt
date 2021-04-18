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

package com.fraktalio.fmodel.example.statestoredsystem.domain

import com.fraktalio.fmodel.domain.Saga

fun restaurantOrderRestaurantSaga() = Saga<RestaurantOrderRestaurantActionResult?, RestaurantOrderRestaurantAction?>(
    react = { e ->
        when (e) {
            is RestaurantOrderPlacedAtRestaurantActionResult -> listOf(
                CreateRestaurantOrderAction(
                    e.restaurantId,
                    e.restaurantOrderId,
                    e.lineItems
                )
            )
            else -> emptyList()
        }
    }
)
