package com.taxiapp.driver_auth.dto.response

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.http.HttpStatus

data class PendingVerificationTO(
    val id: Long,
    val name: String,
    val surname: String,
    val driverLicenceNumber: String,
    val registrationDocumentNumber: String,
    val plateNumber: String,
    val driverLicenseFrontPhotoUrl: String,
    val driverLicenseBackPhotoUrl: String,

    @JsonIgnore
    override val httpStatus: HttpStatus = HttpStatus.OK,
    @JsonIgnore
    override val messages: List<String>? = null,
) : ResultInterface