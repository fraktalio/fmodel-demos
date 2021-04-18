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

package com.fraktalio.fmodel.example.statestoredsystem.adapter.publisher

import arrow.core.Either
import com.fraktalio.fmodel.application.ActionPublisher
import com.fraktalio.fmodel.application.Error
import com.fraktalio.fmodel.application.StateStoredAggregate
import com.fraktalio.fmodel.application.Success
import com.fraktalio.fmodel.example.statestoredsystem.domain.*

class RestaurantOrderRestaurantActionPublisher(
    private val restaurantOrderAggregate: StateStoredAggregate<RestaurantOrderCommand?, RestaurantOrder?, RestaurantOrderEvent?>
) : ActionPublisher<RestaurantOrderRestaurantAction?> {
    override suspend fun RestaurantOrderRestaurantAction?.publish(): Either<Error.PublishingActionFailed<RestaurantOrderRestaurantAction?>, Success.ActionPublishedSuccessfully<RestaurantOrderRestaurantAction?>> =

        when (this) {
            is CreateRestaurantOrderAction -> restaurantOrderAggregate.handle(
                CreateRestaurantOrderCommand(
                    this.restaurantOrderId,
                    this.restaurantId,
                    this.lineItems
                )
            )
                .mapLeft { error ->
                    Error.PublishingActionFailed<RestaurantOrderRestaurantAction?>(
                        this,
                        error.throwable
                    )
                }
                .map { Success.ActionPublishedSuccessfully(this) }

            else -> Either.Right(Success.ActionPublishedSuccessfully(this))
        }

}




