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
import com.fraktalio.fmodel.example.domain.Money
import com.fraktalio.fmodel.example.domain.RestaurantEvent
import com.fraktalio.fmodel.example.domain.RestaurantId
import com.fraktalio.fmodel.example.domain.RestaurantView
import com.fraktalio.fmodel.example.domain.RestaurantView.MenuItem
import com.fraktalio.fmodel.example.domain.RestaurantView.RestaurantMenu
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.util.*


/**
 * Restaurant View repository implementation
 *
 * @property restaurantRepository Restaurant repository
 * @property menuItemRepository Restaurant Menu Item  repository
 * @property operator Spring Reactive [TransactionalOperator]
 * @constructor Create empty Materialized View repository impl
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */

internal open class RestaurantMaterializedViewStateRepositoryImpl(
    private val restaurantRepository: RestaurantCoroutineRepository,
    private val menuItemRepository: MenuItemCoroutineRepository,
    private val operator: TransactionalOperator
) : ViewStateRepository<RestaurantEvent?, RestaurantView?> {

    /**
     * Fetch current state from the repository
     *
     * @return State
     */
    override suspend fun RestaurantEvent?.fetchState(): RestaurantView? {

        val restaurantEntity: RestaurantR2DBCEntity? =
            restaurantRepository.findById(this?.identifier?.identifier.toString())
        val menuItemEntities =
            menuItemRepository.findByRestaurantId(this?.identifier?.identifier.toString())

        return restaurantEntity?.toRestaurant(
            RestaurantMenu(
                UUID.fromString(restaurantEntity.menuId),
                menuItemEntities.map { it.toMenuItem() },
                restaurantEntity.cuisine
            )
        )
    }


    /**
     * Save new state
     *
     * @return new State
     */
    override suspend fun RestaurantView?.save(): RestaurantView? {

        operator.executeAndAwait { transaction ->
            try {
                this?.let { restaurant ->
                    val restaurantEntity = restaurant.toRestaurantEntity()
                    // check if it is Creat or Update
                    restaurantEntity.newRestaurant =
                        !restaurantRepository.existsById(restaurant.id.identifier.toString())
                    val savedRestaurantEntity = restaurantRepository.save(restaurantEntity)
                    restaurant.menu.items.forEach {
                        val menuItemEntity = it.toMenuItemEntity(
                            restaurant.menu.menuId.toString(),
                            restaurant.id.identifier.toString()
                        )
                        // check if it is Create or Update
                        menuItemEntity.newMenuItem = !menuItemRepository.existsById(it.id)
                        val savedMenuItemEntity = menuItemRepository.save(menuItemEntity)
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
        this != null -> RestaurantView(
            RestaurantId(UUID.fromString(this.id)),
            this.name,
            menu,
            this.status
        )
        else -> null
    }

    private fun MenuItemR2DBCEntity.toMenuItem(): MenuItem =
        MenuItem(this.id ?: "", this.menuItemId, this.name, Money(this.price))


    private fun RestaurantView.toRestaurantEntity() = RestaurantR2DBCEntity(
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

}


