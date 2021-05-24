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

package com.fraktalio.fmodel.example.statestoredsystem.application

import com.fraktalio.fmodel.application.StateRepository
import com.fraktalio.fmodel.application.StateStoredAggregate
import com.fraktalio.fmodel.example.statestoredsystem.domain.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.util.*

@SpringBootTest
internal class RestaurantStateStoredAggregateTest(
    @Autowired val restaurantAggregate: StateStoredAggregate<RestaurantCommand?, Restaurant?, RestaurantEvent?>
) : StateRepository<RestaurantCommand?, Restaurant?> by restaurantAggregate {

    val uuid: UUID = UUID.randomUUID()

    @Test
    fun `Assert handling command is successful`() = runBlocking<Unit> {

        // ################ Creating the restaurant ################

        val createRestaurantCommand = CreateRestaurantCommand(
            RestaurantId(), "name", RestaurantMenuVO(
                listOf(MenuItemVO("menu id", "menu item name", Money(BigDecimal.valueOf(1.0)))),
                uuid,
                RestaurantMenuCuisine.GENERAL
            )
        )

        assertThat(restaurantAggregate.handle(createRestaurantCommand).isRight()).isTrue
        val expectedResult = Restaurant(
            createRestaurantCommand.identifier,
            "name",
            Restaurant.RestaurantMenu(
                uuid,
                createRestaurantCommand.menu.menuItems.map { Restaurant.MenuItem(it.id, it.name, it.price) },
                RestaurantMenuCuisine.GENERAL,
                Restaurant.MenuStatus.ACTIVE
            ),
            Restaurant.Status.OPEN
        )
        val actualResult = createRestaurantCommand.fetchState()
        assertThat(actualResult.isRight()).isTrue
        //assertThat(actualResult.orNull()).isEqualTo(expectedResult)

        // ################ Placing the order ################

        val placeRestaurantOrderCommand = PlaceRestaurantOrderCommand(createRestaurantCommand.identifier,
            RestaurantOrderId(),
            createRestaurantCommand.menu.menuItems.map { RestaurantOrderLineItem(1, it.id, it.name) }
        )
        assertThat(restaurantAggregate.handle(placeRestaurantOrderCommand).isRight()).isTrue

        val actualResult2 = placeRestaurantOrderCommand.fetchState()
        assertThat(actualResult.isRight()).isTrue
//        assertThat(actualResult.orNull()).isEqualTo(expectedResult)


//        val expectedOrderResult = RestaurantOrder(
//            placeRestaurantOrderCommand.restaurantOrderIdentifier,
//            placeRestaurantOrderCommand.identifier,
//            RestaurantOrder.Status.CREATED,
//            placeRestaurantOrderCommand.lineItems
//        )

    }

}
