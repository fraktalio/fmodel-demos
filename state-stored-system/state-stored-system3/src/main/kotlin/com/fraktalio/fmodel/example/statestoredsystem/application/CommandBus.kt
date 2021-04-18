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
import arrow.core.computations.either
import com.fraktalio.fmodel.application.Error
import com.fraktalio.fmodel.application.SagaManager
import com.fraktalio.fmodel.application.StateStoredAggregate
import com.fraktalio.fmodel.application.Success
import com.fraktalio.fmodel.example.statestoredsystem.domain.*
import org.springframework.transaction.annotation.Transactional

/**
 * Command bus - entry point of the application layer
 *
 * @property aggregate The main aggregate. It combines all Deciders
 * @property sagaManager The main saga manager. It combines all the Sagas
 * @constructor Creates Command bus
 */
internal open class CommandBus(
    private val aggregate: StateStoredAggregate<Either<RestaurantOrderCommand?, RestaurantCommand?>, Pair<RestaurantOrder?, Restaurant?>, Either<RestaurantOrderEvent?, RestaurantEvent?>>,
    private val sagaManager: SagaManager<Either<RestaurantEvent?, RestaurantOrderEvent?>, Either<RestaurantOrderCommand?, RestaurantCommand?>>
) {
    @Transactional
    open suspend fun publish(command: Either<RestaurantOrderCommand?, RestaurantCommand?>): Either<Error, Success> =

        either {
            val result = aggregate.handle(command).bind()
            result.event.flatMap { sagaManager.handle(it.swap()).bind() }
            result
        }
}

