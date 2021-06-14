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

import com.fraktalio.fmodel.example.statestoredsystem.domain.Restaurant
import com.fraktalio.fmodel.example.statestoredsystem.domain.RestaurantMenuCuisine
import org.springframework.data.repository.PagingAndSortingRepository
import java.math.BigDecimal
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
