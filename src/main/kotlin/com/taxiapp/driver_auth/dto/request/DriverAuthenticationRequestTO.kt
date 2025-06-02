package com.taxiapp.driver_auth.dto.request

import com.taxiapp.driver_auth.dto.AddressTO
import org.springframework.web.multipart.MultipartFile

data class DriverAuthenticationRequestTO(

    val driverLicenceNumber: String,

    val registrationDocumentNumber: String,

    val plateNumber: String,

    val pesel: String,

    val address: AddressTO
)
