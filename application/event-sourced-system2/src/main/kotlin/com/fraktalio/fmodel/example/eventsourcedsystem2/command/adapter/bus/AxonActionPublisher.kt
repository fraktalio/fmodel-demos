package com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.bus

import com.fraktalio.fmodel.application.ActionPublisher
import com.fraktalio.fmodel.example.domain.Command
import com.fraktalio.fmodel.example.eventsourcedsystem2.command.adapter.web.sendCommand
import org.axonframework.commandhandling.gateway.CommandGateway

internal class ActionPublisherImpl(private val commandGateway: CommandGateway) : ActionPublisher<Command> {

    override suspend fun Command.publish(): Command {
        commandGateway.sendCommand<Command, Any>(this)
        return this
    }
}
