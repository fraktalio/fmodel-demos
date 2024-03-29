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

package com.fraktalio.fmodel.example.eventsourcedsystem.query.adapter.persistance

import com.fraktalio.fmodel.application.ViewStateRepository
import com.fraktalio.fmodel.example.domain.*
import com.fraktalio.fmodel.example.domain.RestaurantViewState.MenuItem
import com.fraktalio.fmodel.example.domain.RestaurantViewState.RestaurantMenu
import com.fraktalio.fmodel.example.eventsourcedsystem.command.adapter.getId
import com.fraktalio.fmodel.example.eventsourcedsystem.query.application.MaterializedViewState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.util.*

/**
 * A convenient type alias for ViewStateRepository<Event?, MaterializedViewState>
 */
typealias MaterializedViewStateRepository = ViewStateRepository<Event?, MaterializedViewState>

/**
 * View repository implementation
 *
 * @property restaurantRepository Restaurant repository
 * @property restaurantOrderRepository Restaurant Order repository
 * @property restaurantOrderItemRepository Restaurant Order Item repository
 * @property menuItemRepository Restaurant Menu Item  repository
 * @property operator Spring Reactive [TransactionalOperator]
 * @constructor Create empty Materialized View repository impl
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */

internal open class MaterializedViewStateRepositoryImpl(
    private val restaurantRepository: RestaurantCoroutineRepository,
    private val restaurantOrderRepository: RestaurantOrderCoroutineRepository,
    private val restaurantOrderItemRepository: RestaurantOrderItemCoroutineRepository,
    private val menuItemRepository: MenuItemCoroutineRepository,
    private val operator: TransactionalOperator
) : MaterializedViewStateRepository {

    /**
     * Fetch current state from the repository
     *
     * @return State
     */
    override suspend fun Event?.fetchState(): MaterializedViewState =

        when (this) {
            is RestaurantOrderEvent -> MaterializedViewState(
                null,
                restaurantOrderRepository.findById(getId()).toRestaurantOrder(restaurantOrderItemRepository
                    .findByOrderId(getId())
                    .map { it.toRestaurantOrderLineItem() }
                    .toImmutableList()
                )
            )

            is RestaurantEvent -> {
                val restaurantEntity: RestaurantR2DBCEntity? = restaurantRepository.findById(getId())
                val menuItemEntities = menuItemRepository.findByRestaurantId(getId())
                MaterializedViewState(
                    restaurantEntity?.toRestaurant(
                        RestaurantMenu(
                            UUID.fromString(restaurantEntity.menuId),
                            menuItemEntities.map { it.toMenuItem() }.toImmutableList(),
                            restaurantEntity.cuisine
                        )
                    ),
                    null
                )

            }

            null -> MaterializedViewState(null, null)
        }


    /**
     * Save new state
     *
     * @return new State
     */
    override suspend fun MaterializedViewState.save(): MaterializedViewState {
        operator.executeAndAwait { transaction ->
            try {
                this.order?.let { order ->
                    val restaurantOrderEntity = order.toRestaurantOrderEntity()
                    // check if it is Create or Update
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
                this.restaurant?.let { restaurant ->
                    val restaurantEntity = restaurant.toRestaurantEntity()
                    // check if it is Creat or Update
                    restaurantEntity.newRestaurant =
                        !restaurantRepository.existsById(restaurant.id.identifier.toString())
                    restaurantRepository.save(restaurantEntity)
                    restaurant.menu.items.forEach {
                        val menuItemEntity = it.toMenuItemEntity(
                            restaurant.menu.menuId.toString(),
                            restaurant.id.identifier.toString()
                        )
                        // check if it is Create or Update
                        menuItemEntity.newMenuItem = !menuItemRepository.existsById(it.id)
                        menuItemRepository.save(menuItemEntity)
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

    private fun RestaurantR2DBCEntity?.toRestaurant(menu: RestaurantMenu) = when {
        this != null -> RestaurantViewState(
            RestaurantId(UUID.fromString(this.id)),
            this.name,
            menu,
            this.status
        )

        else -> null
    }


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

    private fun MenuItemR2DBCEntity.toMenuItem(): MenuItem =
        MenuItem(this.id ?: "", this.menuItemId, this.name, Money(this.price))


    private fun RestaurantViewState.toRestaurantEntity() = RestaurantR2DBCEntity(
        this.id.identifier.toString(),
        Long.MIN_VALUE,
        this.name,
        this.status,
        this.menu.cuisine,
        this.menu.status,
        this.menu.menuId.toString(),
    )

    private fun MenuItem.toMenuItemEntity(menuId: String, restaurantId: String) = MenuItemR2DBCEntity(
        this.id, this.menuItemId, menuId, restaurantId, this.name, this.price.amount
    )


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


