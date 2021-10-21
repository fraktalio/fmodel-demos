package com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.axon

import com.fraktalio.fmodel.application.SagaManager
import com.fraktalio.fmodel.application.publishTo
import com.fraktalio.fmodel.example.domain.Command
import com.fraktalio.fmodel.example.domain.Event
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("order-restaurant-saga")
internal class AxonSagaEventHandler(private val sagaManager: SagaManager<Event?, Command?>) {

    @EventHandler
    fun handle(event: Event) {
        runBlocking {
            //sagaManager.handle(event).collect()
            event.publishTo(sagaManager).collect()
        }
    }
}
