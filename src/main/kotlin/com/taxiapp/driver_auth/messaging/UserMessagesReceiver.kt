package com.taxiapp.driver_auth.messaging

import com.taxiapp.driver_auth.dto.event.DriverCreatedEvent
import com.taxiapp.driver_auth.dto.event.DriverFormSubmittedEvent
import com.taxiapp.driver_auth.service.DriverAuthenticationService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@RabbitListener(queues = ["\${rabbit.queue.driver-auth.name}"])
@Component
class UserMessagesReceiver(
    private val driverAuthenticationService: DriverAuthenticationService
) {
    private val logger = LoggerFactory.getLogger(UserMessagesReceiver::class.java)

    @RabbitHandler
    fun receiveUserCreatedEvent(event: DriverCreatedEvent) {
        driverAuthenticationService.createDriverInfo(event)
    }

    @RabbitHandler
    fun receiveFormSubmittedEvent(event: DriverFormSubmittedEvent) {
        driverAuthenticationService.performAutoVerification(event.username)
    }

    @RabbitHandler(isDefault = true)
    fun receiveDefault(event: Any) {
        logger.warn("Received unrouted event: $event")
    }
}