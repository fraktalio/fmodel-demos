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

package com.fraktalio.fmodel.example.eventsourcedsystem3.query.application

import com.fraktalio.fmodel.example.domain.*
import com.fraktalio.fmodel.example.eventsourcedsystem3.query.adapter.persistance.MenuItemCoroutineRepository
import com.fraktalio.fmodel.example.eventsourcedsystem3.query.adapter.persistance.MenuItemR2DBCEntity
import com.fraktalio.fmodel.example.eventsourcedsystem3.query.adapter.persistance.RestaurantCoroutineRepository
import com.fraktalio.fmodel.example.eventsourcedsystem3.query.adapter.persistance.RestaurantR2DBCEntity
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.runBlocking
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.util.*

@Component
@ProcessingGroup("restaurant")
internal class RestaurantEventHandler(
    private val restaurantView: RestaurantView,
    private val restaurantRepository: RestaurantCoroutineRepository,
    private val menuItemRepository: MenuItemCoroutineRepository,
    private val operator: TransactionalOperator
) {
    // ########################################
    // ############## EXTENSIONS ##############
    // ########################################
    private fun RestaurantR2DBCEntity?.toRestaurant(menu: RestaurantViewState.RestaurantMenu) = when {
        this != null -> RestaurantViewState(
            RestaurantId(UUID.fromString(this.id)),
            this.name,
            menu,
            this.status
        )
        else -> null
    }

    private fun MenuItemR2DBCEntity.toMenuItem(): RestaurantViewState.MenuItem =
        RestaurantViewState.MenuItem(this.id ?: "", this.menuItemId, this.name, Money(this.price))


    private fun RestaurantViewState.toRestaurantEntity() = RestaurantR2DBCEntity(
        this.id.identifier.toString(),
        Long.MIN_VALUE,
        this.name,
        this.status,
        this.menu.cuisine,
        this.menu.status,
        this.menu.menuId.toString(),
    )

    private fun RestaurantViewState.MenuItem.toMenuItemEntity(menuId: String, restaurantId: String) =
        MenuItemR2DBCEntity(
            this.id, this.menuItemId, menuId, restaurantId, this.name, this.price.amount
        )

    private suspend fun RestaurantEvent.fetchState(): RestaurantViewState? {
        val restaurantEntity: RestaurantR2DBCEntity? =
            restaurantRepository.findById(this.identifier.identifier.toString())
        val menuItemEntities =
            menuItemRepository.findByRestaurantId(this.identifier.identifier.toString())

        return restaurantEntity?.toRestaurant(
            RestaurantViewState.RestaurantMenu(
                UUID.fromString(restaurantEntity.menuId),
                menuItemEntities.map { it.toMenuItem() }.toImmutableList(),
                restaurantEntity.cuisine
            )
        )
    }

    private suspend fun RestaurantViewState?.save(): RestaurantViewState? {
        operator.executeAndAwait { transaction ->
            try {
                this?.let { restaurant ->
                    val restaurantEntity = restaurant.toRestaurantEntity()
                    // check if it is Create or Update
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


    @EventHandler
    fun handle(event: RestaurantEvent) {
        runBlocking {
            restaurantView.evolve(event.fetchState(), event).save()
        }
    }
}

