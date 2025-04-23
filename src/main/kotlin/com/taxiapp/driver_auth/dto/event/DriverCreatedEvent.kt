package com.taxiapp.driver_auth.dto.event

data class DriverCreatedEvent(
    val driverCreatedEventId: Long,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String?
)
