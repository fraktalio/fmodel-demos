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
import com.fraktalio.fmodel.application.StateStoredOrchestratingAggregate
import com.fraktalio.fmodel.application.handleWithEffect
import com.fraktalio.fmodel.example.domain.*
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.util.*

@SpringBootTest
internal class AggregateTest(
    @Autowired val aggregate: StateStoredOrchestratingAggregate<Command?, AggregateState, Event?>
) : StateRepository<Command?, AggregateState> by aggregate {

    val uuid: UUID = UUID.randomUUID()

    @OptIn(FlowPreview::class)
    @Test
    fun `Assert handling CreateRestaurantCommand command is successful`() = runBlocking<Unit> {

        val createRestaurantCommand = CreateRestaurantCommand(
            RestaurantId(), "name", RestaurantMenuVO(
                persistentListOf(MenuItemVO("menu id", "menu id", "menu item name", Money(BigDecimal.valueOf(1.0)))),
                uuid,
                RestaurantMenuCuisine.GENERAL
            )
        )
        assertThat(aggregate.handleWithEffect(createRestaurantCommand).toEither().isRight()).isTrue


        val expectedResult = Restaurant(
            createRestaurantCommand.identifier,
            "name",
            Restaurant.RestaurantMenu(
                uuid,
                createRestaurantCommand.menu.menuItems.map {
                    Restaurant.MenuItem(
                        it.id,
                        it.menuItemId,
                        it.name,
                        it.price
                    )
                }.toImmutableList(),
                RestaurantMenuCuisine.GENERAL,
                Restaurant.MenuStatus.ACTIVE
            ),
            Restaurant.Status.OPEN
        )
        val actualResult = createRestaurantCommand.fetchState()

        assertThat(actualResult?.restaurant?.id).isEqualTo(expectedResult.id)

    }

    @OptIn(FlowPreview::class)
    @Test
    fun `Assert handling createRestaurantOrderCommand is successful`() = runBlocking<Unit> {

        val createRestaurantOrderCommand = CreateRestaurantOrderCommand(
            RestaurantOrderId(),
            RestaurantId(),
            persistentListOf(RestaurantOrderLineItem("1", 1, "menu id", "menu id"))
        )

        val result = aggregate.handleWithEffect(createRestaurantOrderCommand).toEither()
        result.mapLeft { println(it.throwable?.message) }
        assertThat(result.isRight()).isTrue

        val expectedResult = RestaurantOrder(
            createRestaurantOrderCommand.identifier,
            createRestaurantOrderCommand.restaurantIdentifier,
            RestaurantOrder.Status.CREATED,
            createRestaurantOrderCommand.lineItems
        )
        val actualResult = createRestaurantOrderCommand.fetchState()
        assertThat(actualResult).isEqualTo(AggregateState(expectedResult, null))

    }

    @OptIn(FlowPreview::class)
    @Test
    fun `Assert handling commands is successful`() = runBlocking<Unit> {

        val createRestaurantOrderCommand = CreateRestaurantOrderCommand(
            RestaurantOrderId(),
            RestaurantId(),
            persistentListOf(RestaurantOrderLineItem("1", 1, "menu id", "menu id"))
        )
        val markRestaurantOrderAsPreparedCommand = MarkRestaurantOrderAsPreparedCommand(
            createRestaurantOrderCommand.identifier
        )

        assertThat(aggregate.handleWithEffect(createRestaurantOrderCommand).toEither().isRight()).isTrue
        val expectedResult1 = RestaurantOrder(
            createRestaurantOrderCommand.identifier,
            createRestaurantOrderCommand.restaurantIdentifier,
            RestaurantOrder.Status.CREATED,
            createRestaurantOrderCommand.lineItems
        )
        val actualResult = createRestaurantOrderCommand.fetchState()
        assertThat(actualResult).isEqualTo(AggregateState(expectedResult1, null))


        assertThat(aggregate.handleWithEffect(markRestaurantOrderAsPreparedCommand).toEither().isRight()).isTrue
        val expectedResult2 = RestaurantOrder(
            createRestaurantOrderCommand.identifier,
            createRestaurantOrderCommand.restaurantIdentifier,
            RestaurantOrder.Status.PREPARED,
            createRestaurantOrderCommand.lineItems
        )
        val actualResult2 = markRestaurantOrderAsPreparedCommand.fetchState()
        assertThat(actualResult2).isEqualTo(AggregateState(expectedResult2, null))
    }
}
