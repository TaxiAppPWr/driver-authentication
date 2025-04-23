package com.taxiapp.driver_auth.configuration

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.core.TopicExchange
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MessagingConfig {
    @Value("\${rabbit.exchange.users.name}")
    private val usersExchangeName: String? = null

    @Value("\${rabbit.queue.driver-auth.name}")
    private val driverAuthQueueName: String? = null

    @Value("\${rabbit.topic.drivers.created}")
    private val driverCreatedTopic: String? = null

    @Value("\${rabbit.topic.driver-auth.submitted}")
    private val driverAuthSubmittedTopic: String? = null

    @Bean
    fun exchange(): TopicExchange {
        return TopicExchange("$usersExchangeName")
    }

    @Bean
    fun driverAuthQueue(): Queue {
        return QueueBuilder.durable("$driverAuthQueueName").build()
    }

    @Bean
    fun driverUserBinding(exchange: TopicExchange, driverAuthQueue: Queue): Binding {
        return BindingBuilder.bind(driverAuthQueue).to(exchange).with("$driverCreatedTopic")
    }

    @Bean
    fun driverAuthBinding(exchange: TopicExchange, driverAuthQueue: Queue): Binding {
        return BindingBuilder.bind(driverAuthQueue).to(exchange).with("$driverAuthSubmittedTopic")
    }
}