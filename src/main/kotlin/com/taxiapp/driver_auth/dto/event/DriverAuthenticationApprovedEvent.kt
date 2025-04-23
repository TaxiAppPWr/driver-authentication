package com.taxiapp.driver_auth.dto.event

import com.taxiapp.driver_auth.dto.AddressTO

data class DriverAuthenticationApprovedEvent(
    val driverAuthenticationApprovedId: Long,
    val username: String,
    val firstName: String,
    val lastName: String,
    val driverLicenceNumber: String,
    val registrationDocumentNumber: String,
    val plateNumber: String,
    val pesel: String,
    val address: AddressTO,
)
