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

import com.fraktalio.fmodel.example.domain.Restaurant
import com.fraktalio.fmodel.example.domain.RestaurantMenuCuisine
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.query.Param
import java.math.BigDecimal


internal interface RestaurantCoroutineRepository : CoroutineCrudRepository<RestaurantR2DBCEntity, String>
internal interface MenuItemCoroutineRepository : CoroutineCrudRepository<MenuItemR2DBCEntity, String> {
    // language=SQL
    @Query(
        """
        SELECT * FROM MenuItemR2DBCEntity
        WHERE restaurant_id = :restaurantId
    """
    )
    suspend fun findByRestaurantId(@Param("restaurantId") restaurantId: String): List<MenuItemR2DBCEntity>
}

@Table("RestaurantR2DBCEntity")
internal data class RestaurantR2DBCEntity(
    @Id @Column("id") var aggregateId: String? = null,
    val aggregateVersion: Long,
    val name: String,
    val status: Restaurant.Status,
    val cuisine: RestaurantMenuCuisine,
    val menuStatus: Restaurant.MenuStatus,
    val menuId: String,
    var newRestaurant: Boolean = false
) : Persistable<String> {

    override fun isNew(): Boolean = newRestaurant

    override fun getId(): String? = aggregateId
}

@Table("MenuItemR2DBCEntity")
internal data class MenuItemR2DBCEntity(
    @Id @Column("id") var identifier: String? = null,
    val menuItemId: String,
    val menuId: String,
    val restaurantId: String,
    val name: String,
    val price: BigDecimal,
    var newMenuItem: Boolean = false
) : Persistable<String> {

    override fun isNew(): Boolean = newMenuItem

    override fun getId(): String? = identifier


}
