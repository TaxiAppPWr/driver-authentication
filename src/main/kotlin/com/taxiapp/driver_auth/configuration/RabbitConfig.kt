package com.taxiapp.driver_auth.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.taxiapp.driver_auth.dto.event.DriverCreatedEvent
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class RabbitConfig {

    @Bean
    open fun jsonMessageConverter(): MessageConverter {
        val mapper = ObjectMapper().apply {
            registerKotlinModule()
        }

        val typeMapper = DefaultJackson2JavaTypeMapper().apply {
            setTrustedPackages("*")
            idClassMapping = mapOf(
                "taxiapp.driver.dto.event.DriverCreatedEvent" to DriverCreatedEvent::class.java
            )
        }

        return Jackson2JsonMessageConverter(mapper).apply {
            javaTypeMapper = typeMapper
        }
    }
}