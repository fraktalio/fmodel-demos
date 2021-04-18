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
import java.math.BigDecimal
import java.util.*
import javax.persistence.*

/**
 * Restaurant JPA entity
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 *
 * @property id
 * @property aggregateVersion
 * @property name
 * @property status
 * @property cuisine
 * @property menuStatus
 * @property menuId
 * @property menuItems
 * @constructor Creates Restaurant entity
 */
@Entity
internal data class RestaurantEntity(
    @Id var id: String, var aggregateVersion: Long,
    var name: String,
    var status: Restaurant.Status,
    var cuisine: RestaurantMenuCuisine,
    var menuStatus: Restaurant.MenuStatus,
    var menuId: String,
    @ElementCollection(fetch = FetchType.EAGER) var menuItems: List<MenuItemEmbedable> = mutableListOf()
) {
    constructor() : this(
        "",
        Long.MIN_VALUE,
        "",
        Restaurant.Status.OPEN,
        RestaurantMenuCuisine.GENERAL,
        Restaurant.MenuStatus.ACTIVE,
        "",
        mutableListOf()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RestaurantEntity

        if (id != other.id) return false
        if (aggregateVersion != other.aggregateVersion) return false
        if (name != other.name) return false
        if (status != other.status) return false
        if (cuisine != other.cuisine) return false
        if (menuStatus != other.menuStatus) return false
        if (menuId != other.menuId) return false
        if (menuItems.equals(other.menuItems)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + aggregateVersion.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + cuisine.hashCode()
        result = 31 * result + menuStatus.hashCode()
        result = 31 * result + menuId.hashCode()
        result = 31 * result + menuItems.hashCode()
        return result
    }

}

/**
 * Menu item JPA embedable
 *
 * @property menuId
 * @property name
 * @property price
 * @constructor Creates Menu item embedable
 */
@Embeddable
internal data class MenuItemEmbedable(var menuId: String, var name: String, var price: BigDecimal) {
    constructor() : this("", "", BigDecimal.ZERO)
}

/**
 * Restaurant repository - Spring data repository interface
 *
 * @constructor Creates Spring Data repository
 */
internal interface RestaurantRepository : PagingAndSortingRepository<RestaurantEntity, String>

/**
 * Restaurant state stored aggregate repository implementation
 *
 * @property restaurantRepository Spring repository that is used to implement this repository
 * @constructor Creates Repository
 */
internal class RestaurantStateStoredAggregateRepositoryImpl(private val restaurantRepository: RestaurantRepository) :
    StateRepository<RestaurantCommand?, Restaurant?> {

    override suspend fun RestaurantCommand?.fetchState(): Either<Error.FetchingStateFailed, Restaurant?> =
        Either.catch {
            restaurantRepository.findByIdOrNull(this?.identifier.toString()).toRestaurant()
        }.mapLeft { throwable -> Error.FetchingStateFailed(throwable) }

    override suspend fun Restaurant?.save(): Either<Error.StoringStateFailed<Restaurant?>, Success.StateStoredSuccessfully<Restaurant?>> =
        Either.catch {
            if (this != null) restaurantRepository.save(this.toRestaurantEntity()).toRestaurant()
            Success.StateStoredSuccessfully(this)
        }.mapLeft { throwable -> Error.StoringStateFailed(this, throwable) }

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



