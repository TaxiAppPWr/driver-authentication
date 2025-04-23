package com.taxiapp.driver_auth.dto.response

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.http.HttpStatus

data class PendingVerificationMinimalTO(
    val id: Long,
    val name: String,
    val surname: String,
)

data class PendingVerificationsListTO(
    val verifications: List<PendingVerificationMinimalTO>,

    @JsonIgnore
    override val httpStatus: HttpStatus = HttpStatus.OK,

    @JsonIgnore
    override val messages: List<String>? = null,
) : ResultInterface
