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

package com.fraktalio.fmodel.example.domain

/**
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
import kotlinx.collections.immutable.ImmutableList
import java.math.BigDecimal
import java.util.*

// *************************************** Value objects ********************************************
// **************************************************************************************************

@JvmInline
value class RestaurantId(val identifier: UUID = UUID.randomUUID())

@JvmInline
value class RestaurantOrderId(val identifier: UUID = UUID.randomUUID())

@JvmInline
value class Money(val amount: BigDecimal) {

    operator fun plus(delta: Money): Money {
        return Money(amount.add(delta.amount))
    }

    operator fun times(x: Int): Money {
        return Money(amount.multiply(BigDecimal(x)))
    }
}

data class MenuItemVO(val id: String, val menuItemId: String, val name: String, val price: Money)

enum class RestaurantMenuCuisine {
    SERBIAN,
    ITALIAN,
    INDIAN,
    TURKISH,
    GENERAL
}

data class RestaurantMenuVO(
    val menuItems: ImmutableList<MenuItemVO>,
    val menuId: UUID = UUID.randomUUID(),
    val cuisine: RestaurantMenuCuisine = RestaurantMenuCuisine.GENERAL
)


data class RestaurantOrderLineItem(val id: String, val quantity: Int, val menuItemId: String, val name: String)


// ******************************************** Commands ********************************************
// **************************************************************************************************

sealed class Command {
    abstract val identifierString: String
}

sealed class RestaurantCommand : Command() {
    abstract val identifier: RestaurantId
}

sealed class RestaurantOrderCommand : Command() {
    abstract val identifier: RestaurantOrderId
}

data class CreateRestaurantCommand(
    override val identifier: RestaurantId = RestaurantId(),
    val name: String,
    val menu: RestaurantMenuVO
) : RestaurantCommand() {
    override val identifierString: String
        get() = identifier.identifier.toString()
}

data class ChangeRestaurantMenuCommand(
    override val identifier: RestaurantId = RestaurantId(),
    val menu: RestaurantMenuVO
) : RestaurantCommand() {
    override val identifierString: String
        get() = identifier.identifier.toString()
}

data class ActivateRestaurantMenuCommand(
    override val identifier: RestaurantId,
    val menuId: UUID
) : RestaurantCommand() {
    override val identifierString: String
        get() = identifier.identifier.toString()
}

data class PassivateRestaurantMenuCommand(
    override val identifier: RestaurantId,
    val menuId: UUID
) : RestaurantCommand() {
    override val identifierString: String
        get() = identifier.identifier.toString()
}

data class PlaceRestaurantOrderCommand(
    override val identifier: RestaurantId,
    val restaurantOrderIdentifier: RestaurantOrderId = RestaurantOrderId(),
    val lineItems: ImmutableList<RestaurantOrderLineItem>
) : RestaurantCommand() {
    override val identifierString: String
        get() = identifier.identifier.toString()
}

data class CreateRestaurantOrderCommand(
    override val identifier: RestaurantOrderId,
    val restaurantIdentifier: RestaurantId = RestaurantId(),
    val lineItems: ImmutableList<RestaurantOrderLineItem>
) : RestaurantOrderCommand() {
    override val identifierString: String
        get() = identifier.identifier.toString()
}

data class MarkRestaurantOrderAsPreparedCommand(
    override val identifier: RestaurantOrderId
) : RestaurantOrderCommand() {
    override val identifierString: String
        get() = identifier.identifier.toString()
}


// ********************************************** Events ********************************************
// **************************************************************************************************

sealed class Event {
    abstract val identifierString: String
}

sealed class RestaurantEvent : Event() {
    abstract val identifier: RestaurantId
}

sealed class RestaurantErrorEvent : RestaurantEvent() {
    abstract val reason: String
}

sealed class RestaurantOrderEvent : Event() {
    abstract val identifier: RestaurantOrderId
}

sealed class RestaurantOrderErrorEvent : RestaurantOrderEvent() {
    abstract val reason: String
}

// Restaurant

data class RestaurantCreatedEvent(
    override val identifier: RestaurantId,
    val name: String,
    val menu: RestaurantMenuVO
) : RestaurantEvent() {
    override val identifierString: String
        get() = identifier.identifier.toString()
}

data class RestaurantNotCreatedEvent(
    override val identifier: RestaurantId,
    val name: String,
    val menu: RestaurantMenuVO,
    override val reason: String
) : RestaurantErrorEvent() {
    override val identifierString: String
        get() = identifier.identifier.toString()
}

data class RestaurantMenuChangedEvent(
    override val identifier: RestaurantId,
    val menu: RestaurantMenuVO
) : RestaurantEvent() {
    override val identifierString: String
        get() = identifier.identifier.toString()
}

data class RestaurantMenuNotChangedEvent(
    override val identifier: RestaurantId,
    val menu: RestaurantMenuVO,
    override val reason: String
) : RestaurantErrorEvent() {
    override val identifierString: String
        get() = identifier.identifier.toString()
}

data class RestaurantMenuActivatedEvent(
    override val identifier: RestaurantId,
    val menuId: UUID,
) : RestaurantEvent() {
    override val identifierString: String
        get() = identifier.identifier.toString()
}

data class RestaurantMenuNotActivatedEvent(
    override val identifier: RestaurantId,
    val menuId: UUID,
    override val reason: String
) : RestaurantErrorEvent() {
    override val identifierString: String
        get() = identifier.identifier.toString()
}

data class RestaurantMenuPassivatedEvent(
    override val identifier: RestaurantId,
    val menuId: UUID,
) : RestaurantEvent() {
    override val identifierString: String
        get() = identifier.identifier.toString()
}

data class RestaurantMenuNotPassivatedEvent(
    override val identifier: RestaurantId,
    val menuId: UUID,
    override val reason: String
) : RestaurantErrorEvent() {
    override val identifierString: String
        get() = identifier.identifier.toString()
}

data class RestaurantOrderPlacedAtRestaurantEvent(
    override val identifier: RestaurantId,
    val lineItems: ImmutableList<RestaurantOrderLineItem>,
    val restaurantOrderId: RestaurantOrderId
) : RestaurantEvent() {
    override val identifierString: String
        get() = identifier.identifier.toString()
}

data class RestaurantOrderNotPlacedAtRestaurantEvent(
    override val identifier: RestaurantId,
    val lineItems: ImmutableList<RestaurantOrderLineItem>,
    val restaurantOrderId: RestaurantOrderId,
    override val reason: String
) : RestaurantErrorEvent() {
    override val identifierString: String
        get() = identifier.identifier.toString()
}

data class RestaurantOrderRejectedByRestaurantEvent(
    override val identifier: RestaurantId,
    val restaurantOrderId: RestaurantOrderId,
    override val reason: String
) : RestaurantErrorEvent() {
    override val identifierString: String
        get() = identifier.identifier.toString()
}

// Restaurant Order

data class RestaurantOrderCreatedEvent(
    override val identifier: RestaurantOrderId,
    val lineItems: ImmutableList<RestaurantOrderLineItem>,
    val restaurantId: RestaurantId
) : RestaurantOrderEvent() {
    override val identifierString: String
        get() = identifier.identifier.toString()
}

data class RestaurantOrderPreparedEvent(
    override val identifier: RestaurantOrderId,
) : RestaurantOrderEvent() {
    override val identifierString: String
        get() = identifier.identifier.toString()
}

data class RestaurantOrderNotPreparedEvent(
    override val identifier: RestaurantOrderId,
    override val reason: String
) : RestaurantOrderErrorEvent() {
    override val identifierString: String
        get() = identifier.identifier.toString()
}

data class RestaurantOrderRejectedEvent(
    override val identifier: RestaurantOrderId,
    override val reason: String
) : RestaurantOrderErrorEvent() {
    override val identifierString: String
        get() = identifier.identifier.toString()
}
