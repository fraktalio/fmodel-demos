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
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.findByIdOrNull
import java.util.*
import javax.persistence.*

@Entity
internal data class RestaurantOrderEntity(
    @Id var id: String,
    var aggregateVersion: Long,
    @ElementCollection(fetch = FetchType.EAGER) var lineItems: List<RestaurantOrderItemEmbeddable> = mutableListOf(),
    var restaurantId: String,
    var state: RestaurantOrder.Status
) {
    constructor() : this(
        "",
        Long.MIN_VALUE,
        mutableListOf(),
        "",
        RestaurantOrder.Status.CREATED
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RestaurantOrderEntity

        if (id != other.id) return false
        if (aggregateVersion != other.aggregateVersion) return false
        if (lineItems.equals(other.lineItems)) return false
        if (restaurantId != other.restaurantId) return false
        if (state != other.state) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + aggregateVersion.hashCode()
        result = 31 * result + lineItems.hashCode()
        result = 31 * result + restaurantId.hashCode()
        result = 31 * result + state.hashCode()
        return result
    }

}

@Embeddable
internal data class RestaurantOrderItemEmbeddable(var menuId: String, var name: String, var quantity: Int) {
    constructor() : this("", "", Int.MIN_VALUE)
}

/**
 * Restaurant order repository - Spring data repository interface
 *
 * @constructor Creates Spring Data repository
 */
internal interface RestaurantOrderRepository : PagingAndSortingRepository<RestaurantOrderEntity, String>

/**
 * Restaurant Order state stored aggregate repository implementation
 *
 * @property restaurantOrderRepository Spring repository that is used to implement this repository
 * @constructor Creates Repository
 */
internal class RestaurantOrderStateStoredAggregateRepositoryImpl(private val restaurantOrderRepository: RestaurantOrderRepository) :
    StateRepository<RestaurantOrderCommand?, RestaurantOrder?> {

    override suspend fun RestaurantOrderCommand?.fetchState(): Either<Error.FetchingStateFailed, RestaurantOrder?> =
        Either.catch {
            restaurantOrderRepository.findByIdOrNull(this?.identifier.toString()).toRestaurantOrder()
        }.mapLeft { throwable -> Error.FetchingStateFailed(throwable) }


    override suspend fun RestaurantOrder?.save(): Either<Error.StoringStateFailed<RestaurantOrder?>, Success.StateStoredSuccessfully<RestaurantOrder?>> =
        Either.catch {
            if (this != null) restaurantOrderRepository.save(this.toRestaurantOrderEntity()).toRestaurantOrder()
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
}


