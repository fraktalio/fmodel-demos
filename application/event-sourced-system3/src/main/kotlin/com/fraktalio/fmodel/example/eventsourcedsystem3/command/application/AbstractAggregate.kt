package com.fraktalio.fmodel.example.eventsourcedsystem3.command.application

import com.fraktalio.fmodel.domain.Decider
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle

abstract class AbstractAggregate<C, S, E> {
    abstract val decider: Decider<C, S, E>
    abstract var state: S
    abstract fun E.getAggregateIdentifier(): String?

    @AggregateIdentifier
    private var id: String? = null

    protected fun handleIt(command: C) {
        runBlocking {
            decider.decide(command, state).collect {
                if (it != null) AggregateLifecycle.apply(it)
            }
        }
    }

    @EventSourcingHandler
    fun on(event: E) {
        runBlocking {
            id = event.getAggregateIdentifier()
            state = decider.evolve(state, event)
        }
    }
}
