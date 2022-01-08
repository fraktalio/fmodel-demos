package com.fraktalio.fmodel.example.eventsourcedsystem3.command.application

import com.fraktalio.fmodel.domain.combine
import com.fraktalio.fmodel.example.domain.Command
import com.fraktalio.fmodel.example.domain.Event
import com.fraktalio.fmodel.example.domain.RestaurantOrderSaga
import com.fraktalio.fmodel.example.domain.RestaurantSaga
import kotlinx.coroutines.runBlocking
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("order-restaurant-saga")
internal class OrderRestaurantSaga(
    private val restaurantSaga: RestaurantSaga,
    private val restaurantOrderSaga: RestaurantOrderSaga,
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
