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

import com.fraktalio.fmodel.example.statestoredsystem.domain.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.util.*

@SpringBootTest
internal class CommandBusTest(@Autowired val commandBus: CommandBus) {

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

        assertThat(commandBus.publish(createRestaurantCommand).isRight()).isTrue

    }

    @Test
    fun `Assert handling PlaceRestaurantOrderCommand command is successful`() = runBlocking<Unit> {

        val placeRestaurantOrderCommand = PlaceRestaurantOrderCommand(
            RestaurantId(),
            RestaurantOrderId(),
            listOf(RestaurantOrderLineItem(1, UUID.randomUUID().toString(), "name"))
        )

        assertThat(commandBus.publish(placeRestaurantOrderCommand).isRight()).isTrue

    }

    @Test
    fun `Assert handling createRestaurantOrderCommand is successful`() = runBlocking<Unit> {

        val createRestaurantOrderCommand = CreateRestaurantOrderCommand(
            RestaurantOrderId(),
            RestaurantId(),
            listOf(RestaurantOrderLineItem(1, UUID.randomUUID().toString(), "name"))
        )

        assertThat(commandBus.publish(createRestaurantOrderCommand).isRight()).isTrue

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

        assertThat(commandBus.publish(createRestaurantOrderCommand).isRight()).isTrue
        assertThat(commandBus.publish(markRestaurantOrderAsPreparedCommand).isRight()).isTrue

    }

}
