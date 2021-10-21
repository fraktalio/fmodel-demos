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

import com.fraktalio.fmodel.example.domain.RestaurantOrderView
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.query.Param


internal interface RestaurantOrderCoroutineRepository : CoroutineCrudRepository<RestaurantOrderR2DBCEntity, String>
internal interface RestaurantOrderItemCoroutineRepository :
    CoroutineCrudRepository<RestaurantOrderItemR2DBCEntity, String> {

    // language=SQL
    @Query(
        """
        SELECT * FROM RestaurantOrderItemR2DBCEntity
        WHERE order_id = :order_id
    """
    )
    suspend fun findByOrderId(@Param("order_id") order_id: String): List<RestaurantOrderItemR2DBCEntity>
}

@Table("RestaurantOrderR2DBCEntity")
internal data class RestaurantOrderR2DBCEntity(
    @Id @Column("id") var aggregateId: String? = null,
    val aggregateVersion: Long,
    val restaurantId: String,
    val state: RestaurantOrderView.Status,
    var newRestaurantOrder: Boolean = false
) : Persistable<String> {

    override fun isNew(): Boolean = newRestaurantOrder

    override fun getId(): String? = aggregateId
}

@Table("RestaurantOrderItemR2DBCEntity")
internal data class RestaurantOrderItemR2DBCEntity(
    @Id @Column("id") var identifier: String? = null,
    val orderId: String,
    val menuItemId: String,
    val name: String,
    val quantity: Int,
    var newRestaurantOrderItem: Boolean = false
) : Persistable<String> {

    override fun isNew(): Boolean = newRestaurantOrderItem

    override fun getId(): String? = identifier
}

