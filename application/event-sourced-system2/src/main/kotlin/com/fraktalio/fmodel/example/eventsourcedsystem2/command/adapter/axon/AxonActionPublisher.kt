package com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.axon

import com.fraktalio.fmodel.application.ActionPublisher
import com.fraktalio.fmodel.example.domain.Command
import org.axonframework.commandhandling.gateway.CommandGateway

internal class ActionPublisherImpl(private val commandGateway: CommandGateway) : ActionPublisher<Command?> {

    override suspend fun Command?.publish(): Command? {
        if (this != null) commandGateway.sendAndWait<Command>(this)
        return this
    }
}
