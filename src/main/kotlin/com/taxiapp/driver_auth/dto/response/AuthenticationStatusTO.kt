package com.taxiapp.driver_auth.dto.response

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.http.HttpStatus

data class AuthenticationStatusTO(
    val status: String,

    @JsonIgnore
    override val httpStatus: HttpStatus = HttpStatus.OK,
    @JsonIgnore
    override val messages: List<String>? = null,

) : ResultInterface
