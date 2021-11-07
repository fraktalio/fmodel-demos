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

package com.fraktalio.fmodel.example.eventsourcedsystem2.query.adapter.persistance

import com.fraktalio.fmodel.application.ViewStateRepository
import com.fraktalio.fmodel.example.domain.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.util.*

/**
 * A convenient type alias for ViewStateRepository<RestaurantOrderEvent?, RestaurantOrderViewState?>
 */
typealias RestaurantOrderMaterializedViewStateRepository = ViewStateRepository<RestaurantOrderEvent?, RestaurantOrderViewState?>

/**
 * Restaurant Order View repository implementation
 *
 * @property restaurantOrderRepository Restaurant Order repository
 * @property restaurantOrderItemRepository Restaurant Order Item repository
 * @property operator Spring Reactive [TransactionalOperator]
 * @constructor Create empty Materialized View repository impl
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */

internal open class RestaurantOrderMaterializedViewStateRepositoryImpl(
    private val restaurantOrderRepository: RestaurantOrderCoroutineRepository,
    private val restaurantOrderItemRepository: RestaurantOrderItemCoroutineRepository,
    private val operator: TransactionalOperator
) : ViewStateRepository<RestaurantOrderEvent?, RestaurantOrderViewState?> {

    /**
     * Fetch current state from the repository
     *
     * @return State
     */
    override suspend fun RestaurantOrderEvent?.fetchState(): RestaurantOrderViewState? =
        restaurantOrderRepository.findById(this?.identifier?.identifier.toString())
            .toRestaurantOrder(
                restaurantOrderItemRepository.findByOrderId(this?.identifier?.identifier.toString())
                    .map { it.toRestaurantOrderLineItem() }.toImmutableList()
            )


    /**
     * Save new state
     *
     * @return new State
     */
    override suspend fun RestaurantOrderViewState?.save(): RestaurantOrderViewState? {

        operator.executeAndAwait { transaction ->
            try {

                this?.let { order ->
                    val restaurantOrderEntity = order.toRestaurantOrderEntity()
                    // check if it is Creat or Update
                    restaurantOrderEntity.newRestaurantOrder =
                        !restaurantOrderRepository.existsById(order.id.identifier.toString())
                    restaurantOrderRepository.save(restaurantOrderEntity)
                    order.lineItems.forEach {
                        val orderItemEntity = it.toRestaurantOrderItemEntity(order.id.identifier.toString())
                        // check if it is Create or Update
                        orderItemEntity.newRestaurantOrderItem = !restaurantOrderItemRepository.existsById(it.id)
                        restaurantOrderItemRepository.save(orderItemEntity)
                    }

                }

            } catch (e: Exception) {
                transaction.setRollbackOnly()
                throw e
            }

        }
        return this
    }


    // EXTENSIONS

    private fun RestaurantOrderR2DBCEntity?.toRestaurantOrder(lineItems: ImmutableList<RestaurantOrderLineItem>): RestaurantOrderViewState? =
        when {
            this != null -> RestaurantOrderViewState(
                RestaurantOrderId(UUID.fromString(this.id)),
                RestaurantId(UUID.fromString(this.restaurantId)),
                this.state,
                lineItems
            )
            else -> null
        }

    private fun RestaurantOrderItemR2DBCEntity.toRestaurantOrderLineItem(): RestaurantOrderLineItem =
        RestaurantOrderLineItem(this.id ?: "", this.quantity, this.menuItemId, this.name)


    private fun RestaurantOrderViewState.toRestaurantOrderEntity() = RestaurantOrderR2DBCEntity(
        this.id.identifier.toString(),
        Long.MIN_VALUE,
        this.restaurantId.identifier.toString(),
        this.status
    )

    private fun RestaurantOrderLineItem.toRestaurantOrderItemEntity(orderId: String) = RestaurantOrderItemR2DBCEntity(
        this.id,
        orderId,
        this.menuItemId,
        this.name,
        this.quantity
    )


}


