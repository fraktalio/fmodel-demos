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

/**
 * Action publisher
 *
 * This publisher is explicitly calling an aggregate to handle this Action as command.
 * In the a distributed environments this actions could resolve in a serialized message that is routed over the wire, enabling location transparency.
 *
 * @property aggregate Aggregate
 * @constructor Creates Action publisher
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
internal class ActionPublisher(
    private val aggregate: StateStoredAggregate<Either<RestaurantOrderCommand?, RestaurantCommand?>, Pair<RestaurantOrder?, Restaurant?>, Either<RestaurantOrderEvent?, RestaurantEvent?>>
) : ActionPublisher<Either<RestaurantOrderCommand?, RestaurantCommand?>> {

    override suspend fun Either<RestaurantOrderCommand?, RestaurantCommand?>.publish(): Either<Error.PublishingActionFailed<Either<RestaurantOrderCommand?, RestaurantCommand?>>, Success.ActionPublishedSuccessfully<Either<RestaurantOrderCommand?, RestaurantCommand?>>> =

        aggregate.handle(this)
            .mapLeft { error ->
                Error.PublishingActionFailed(
                    this,
                    error.throwable
                )
            }
            .map { Success.ActionPublishedSuccessfully(this) }


}




