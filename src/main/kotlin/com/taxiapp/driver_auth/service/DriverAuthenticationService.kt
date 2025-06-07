package com.taxiapp.driver_auth.service

import com.taxiapp.driver_auth.dto.event.DriverCreatedEvent
import com.taxiapp.driver_auth.dto.request.DriverAuthenticationRequestTO
import com.taxiapp.driver_auth.dto.response.ResultInterface
import org.springframework.web.multipart.MultipartFile

interface DriverAuthenticationService {
    fun getAuthenticationStatus(username: String): ResultInterface
    fun submitDriverAuthentication(
        username: String,
        authenticationRequestTO: DriverAuthenticationRequestTO,
        licenseFrontPhoto: MultipartFile,
        licenseBackPhoto: MultipartFile,
    ): ResultInterface
    fun cancelDriverAuthentication(
        username: String,
    ): ResultInterface
    fun getPendingVerifications(): ResultInterface
    fun getPendingVerificationById(id: Long): ResultInterface
    fun approveVerification(id: Long): ResultInterface
    fun rejectVerification(id: Long): ResultInterface
    fun createDriverInfo(userEvent: DriverCreatedEvent)
    fun performAutoVerification(username: String)
    fun getFirstPendingVerification(): ResultInterface
}