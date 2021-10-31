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

import com.fraktalio.fmodel.application.StateRepository
import com.fraktalio.fmodel.example.domain.*
import com.fraktalio.fmodel.example.statestoredsystem.application.AggregateState
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.util.*

typealias AggregateStateStoredRepository = StateRepository<Command?, AggregateState>

/**
 * Aggregate repository implementation
 *
 * @property restaurantRepository Restaurant repository
 * @property restaurantOrderRepository Restaurant Order repository
 * @property restaurantOrderItemRepository Restaurant Order Item repository
 * @property menuItemRepository Restaurant Menu Item  repository
 * @property operator Spring Reactive [TransactionalOperator]
 * @constructor Create empty Aggregate repository impl
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */

internal open class AggregateStateStoredRepositoryImpl(
    private val restaurantRepository: RestaurantCoroutineRepository,
    private val restaurantOrderRepository: RestaurantOrderCoroutineRepository,
    private val restaurantOrderItemRepository: RestaurantOrderItemCoroutineRepository,
    private val menuItemRepository: MenuItemCoroutineRepository,
    private val operator: TransactionalOperator
) : AggregateStateStoredRepository {

    /**
     * Fetch current state from the repository
     *
     * @return State
     */
    override suspend fun Command?.fetchState(): AggregateState =

        when (this) {

            is RestaurantOrderCommand -> AggregateState(
                restaurantOrderRepository.findById(this.identifier.identifier.toString()).toRestaurantOrder(
                    restaurantOrderItemRepository.findByOrderId(this.identifier.identifier.toString())
                        .map { it.toRestaurantOrderLineItem() }
                ),
                null
            )

            is RestaurantCommand -> {
                val restaurantEntity: RestaurantR2DBCEntity? =
                    restaurantRepository.findById(this.identifier.identifier.toString())
                val menuItemEntities =
                    menuItemRepository.findByRestaurantId(this.identifier.identifier.toString())
                AggregateState(
                    null,
                    restaurantEntity?.toRestaurant(
                        Restaurant.RestaurantMenu(
                            UUID.fromString(restaurantEntity.menuId),
                            menuItemEntities.map { it.toMenuItem() },
                            restaurantEntity.cuisine
                        )
                    )
                )

            }

            null -> throw UnsupportedOperationException()
        }


    /**
     * Save new state
     *
     * @return new State
     */
    override suspend fun AggregateState.save(): AggregateState {

        operator.executeAndAwait { transaction ->
            try {

                this.order?.let { order ->
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

    private fun RestaurantR2DBCEntity?.toRestaurant(menu: Restaurant.RestaurantMenu) = when {
        this != null -> Restaurant(
            RestaurantId(UUID.fromString(this.id)),
            this.name,
            menu,
            this.status
        )
        else -> null
    }


    private fun RestaurantOrderR2DBCEntity?.toRestaurantOrder(lineItems: List<RestaurantOrderLineItem>): RestaurantOrder? =
        when {
            this != null -> RestaurantOrder(
                RestaurantOrderId(UUID.fromString(this.id)),
                RestaurantId(UUID.fromString(this.restaurantId)),
                this.state,
                lineItems
            )
            else -> null
        }

    private fun RestaurantOrderItemR2DBCEntity.toRestaurantOrderLineItem(): RestaurantOrderLineItem =
        RestaurantOrderLineItem(this.id ?: "", this.quantity, this.menuItemId, this.name)

    private fun MenuItemR2DBCEntity.toMenuItem(): Restaurant.MenuItem =
        Restaurant.MenuItem(this.id ?: "", this.menuItemId, this.name, Money(this.price))


    private fun Restaurant.toRestaurantEntity() = RestaurantR2DBCEntity(
        this.id.identifier.toString(),
        Long.MIN_VALUE,
        this.name,
        this.status,
        this.menu.cuisine,
        this.menu.status,
        this.menu.menuId.toString(),
    )

    private fun Restaurant.MenuItem.toMenuItemEntity(menuId: String, restaurantId: String) = MenuItemR2DBCEntity(
        this.id, this.menuItemId, menuId, restaurantId, this.name, this.price.amount
    )


    private fun RestaurantOrder.toRestaurantOrderEntity() = RestaurantOrderR2DBCEntity(
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


