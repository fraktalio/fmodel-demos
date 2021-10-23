package com.fraktalio.fmodel.example.eventsourcedsystem3.command.application

import com.fraktalio.fmodel.domain.Saga
import com.fraktalio.fmodel.domain.combine
import com.fraktalio.fmodel.example.domain.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("order-restaurant-saga")
internal class OrderRestaurantSaga(
    private val restaurantSaga: Saga<RestaurantOrderEvent?, RestaurantCommand?>,
    private val restaurantOrderSaga: Saga<RestaurantEvent?, RestaurantOrderCommand?>,
    private val commandGateway: CommandGateway
) {

    @EventHandler
    fun handle(event: Event) {
        runBlocking {
            restaurantSaga
                .combine(restaurantOrderSaga)
                .react(event)
                .collect {
                    if (it != null) commandGateway.send<Command>(it)
                }
        }
    }
}
