package com.taxiapp.driver_auth.dto.event

data class DriverAuthenticationFailedEvent(
    val userAuthenticationFailedEventId: Long,
    val username: String,
    val autoVerification: Boolean
)