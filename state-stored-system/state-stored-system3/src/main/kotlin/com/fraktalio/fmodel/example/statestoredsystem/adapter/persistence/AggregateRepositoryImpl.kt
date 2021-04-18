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

package com.fraktalio.fmodel.example.statestoredsystem.adapter.persistence

import arrow.core.Either
import com.fraktalio.fmodel.application.Error
import com.fraktalio.fmodel.application.StateRepository
import com.fraktalio.fmodel.application.Success
import com.fraktalio.fmodel.example.statestoredsystem.domain.*
import org.springframework.data.repository.findByIdOrNull
import java.util.*


internal class AggregateRepositoryImpl(
    private val restaurantRepository: RestaurantRepository,
    private val restaurantOrderRepository: RestaurantOrderRepository
) : StateRepository<Either<RestaurantOrderCommand?, RestaurantCommand?>, Pair<RestaurantOrder?, Restaurant?>> {

    override suspend fun Either<RestaurantOrderCommand?, RestaurantCommand?>.fetchState(): Either<Error.FetchingStateFailed, Pair<RestaurantOrder?, Restaurant?>?> =

        Either.catch {
            when (this) {
                is Either.Left -> Pair(
                    this.value?.let {
                        restaurantOrderRepository.findByIdOrNull(it.identifier.identifier.toString())
                            .toRestaurantOrder()
                    },
                    null
                )
                is Either.Right -> Pair(
                    null,
                    this.value?.let {
                        restaurantRepository.findByIdOrNull(it.identifier.identifier.toString()).toRestaurant()
                    }
                )
            }
        }.mapLeft { throwable -> Error.FetchingStateFailed(throwable) }


    override suspend fun Pair<RestaurantOrder?, Restaurant?>.save(): Either<Error.StoringStateFailed<Pair<RestaurantOrder?, Restaurant?>>, Success.StateStoredSuccessfully<Pair<RestaurantOrder?, Restaurant?>>> =

        Either.catch {
            this.first?.let {
                restaurantOrderRepository.save(it.toRestaurantOrderEntity()).toRestaurantOrder()
            }
            this.second?.let {
                restaurantRepository.save(it.toRestaurantEntity()).toRestaurant()
            }
            Success.StateStoredSuccessfully(this)
        }.mapLeft { throwable -> Error.StoringStateFailed(this, throwable) }


    private fun RestaurantOrder.toRestaurantOrderEntity(): RestaurantOrderEntity = RestaurantOrderEntity(
        this.id.identifier.toString(),
        Long.MIN_VALUE,
        this.lineItems.map { RestaurantOrderItemEmbeddable(it.menuItemId, it.name, it.quantity) },
        this.restaurantId.identifier.toString(),
        this.status
    )

    private fun RestaurantOrderEntity?.toRestaurantOrder(): RestaurantOrder? = when {
        this != null -> RestaurantOrder(
            RestaurantOrderId(UUID.fromString(this.id)),
            RestaurantId(UUID.fromString(this.restaurantId)),
            this.state,
            this.lineItems.map { RestaurantOrderLineItem(it.quantity, it.menuId, it.name) }
        )
        else -> null
    }

    private fun Restaurant.toRestaurantEntity() = RestaurantEntity(
        this.id.identifier.toString(),
        Long.MIN_VALUE,
        this.name,
        this.status,
        this.menu.cuisine,
        this.menu.status,
        this.menu.menuId.toString(),
        this.menu.items.map { MenuItemEmbedable(it.id, it.name, it.price.amount) })

    private fun RestaurantEntity?.toRestaurant() = when {
        this != null -> Restaurant(
            RestaurantId(UUID.fromString(this.id)),
            this.name,
            Restaurant.RestaurantMenu(
                UUID.fromString(this.menuId),
                this.menuItems.map { menuItemEmbedable ->
                    Restaurant.MenuItem(
                        menuItemEmbedable.menuId,
                        menuItemEmbedable.name,
                        Money(menuItemEmbedable.price)
                    )
                },
                this.cuisine,
                this.menuStatus
            ),
            this.status
        )
        else -> null
    }

}


