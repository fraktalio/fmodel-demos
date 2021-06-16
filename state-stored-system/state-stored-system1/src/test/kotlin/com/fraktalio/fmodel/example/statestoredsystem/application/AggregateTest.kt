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
import com.fraktalio.fmodel.example.domain.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.util.*

@SpringBootTest
internal class AggregateTest(
    @Autowired val aggregate: StateStoredAggregate<Command?, Pair<RestaurantOrder?, Restaurant?>, Event?>
) : StateRepository<Command?, Pair<RestaurantOrder?, Restaurant?>> by aggregate {

    val uuid: UUID = UUID.randomUUID()

    @Test
    fun `Assert handling CreateRestaurantCommand command is successful`() = runBlocking<Unit> {

        val createRestaurantCommand = CreateRestaurantCommand(
            RestaurantId(), "name", RestaurantMenuVO(
                listOf(MenuItemVO("menu id", "menu item name", Money(BigDecimal.valueOf(1.0)))),
                uuid,
                RestaurantMenuCuisine.GENERAL
            )
        )

        assertThat(aggregate.handle(createRestaurantCommand).isRight()).isTrue

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
        assertThat(actualResult.orNull()).isEqualTo(Pair(null, expectedResult))

    }

    @Test
    fun `Assert handling createRestaurantOrderCommand is successful`() = runBlocking<Unit> {

        val createRestaurantOrderCommand = CreateRestaurantOrderCommand(
            RestaurantOrderId(),
            RestaurantId(),
            listOf(RestaurantOrderLineItem(1, UUID.randomUUID().toString(), "name"))
        )

        assertThat(aggregate.handle(createRestaurantOrderCommand).isRight()).isTrue

        val expectedResult = RestaurantOrder(
            createRestaurantOrderCommand.identifier,
            createRestaurantOrderCommand.restaurantIdentifier,
            RestaurantOrder.Status.CREATED,
            createRestaurantOrderCommand.lineItems
        )
        val actualResult = createRestaurantOrderCommand.fetchState()
        assertThat(actualResult.isRight()).isTrue
        assertThat(actualResult.orNull()).isEqualTo(Pair(expectedResult, null))

    }

    @Test
    fun `Assert handling commands is successful`() = runBlocking<Unit> {

        val createRestaurantOrderCommand = CreateRestaurantOrderCommand(
            RestaurantOrderId(),
            RestaurantId(),
            listOf(RestaurantOrderLineItem(1, UUID.randomUUID().toString(), "name"))
        )
        val markRestaurantOrderAsPreparedCommand = MarkRestaurantOrderAsPreparedCommand(
            createRestaurantOrderCommand.identifier
        )

        assertThat(aggregate.handle(createRestaurantOrderCommand).isRight()).isTrue
        val expectedResult1 = RestaurantOrder(
            createRestaurantOrderCommand.identifier,
            createRestaurantOrderCommand.restaurantIdentifier,
            RestaurantOrder.Status.CREATED,
            createRestaurantOrderCommand.lineItems
        )
        val actualResult = createRestaurantOrderCommand.fetchState()
        assertThat(actualResult.isRight()).isTrue
        assertThat(actualResult.orNull()).isEqualTo(Pair(expectedResult1, null))


        assertThat(aggregate.handle(markRestaurantOrderAsPreparedCommand).isRight()).isTrue
        val expectedResult2 = RestaurantOrder(
            createRestaurantOrderCommand.identifier,
            createRestaurantOrderCommand.restaurantIdentifier,
            RestaurantOrder.Status.PREPARED,
            createRestaurantOrderCommand.lineItems
        )
        val actualResult2 = markRestaurantOrderAsPreparedCommand.fetchState()
        assertThat(actualResult2.isRight()).isTrue
        assertThat(actualResult2.orNull()).isEqualTo(Pair(expectedResult2, null))


    }

}
