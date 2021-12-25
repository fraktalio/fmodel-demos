package com.fraktalio.fmodel.example.eventsourcedsystem.command.adapter.web

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.runInterruptible
import org.axonframework.commandhandling.gateway.CommandGateway

suspend fun <C : Any, R : Any?> CommandGateway.sendCommand(command: C) = runInterruptible(currentCoroutineContext()) {
    sendAndWait<R>(command)
}
