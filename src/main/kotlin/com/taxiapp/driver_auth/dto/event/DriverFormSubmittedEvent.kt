package com.taxiapp.driver_auth.dto.event

data class DriverFormSubmittedEvent(
    val userFormSubmittedEventId: Long,
    val username: String
)