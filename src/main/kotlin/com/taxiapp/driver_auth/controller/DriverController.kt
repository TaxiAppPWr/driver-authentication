package com.taxiapp.driver_auth.controller

import com.taxiapp.driver_auth.dto.request.DriverAuthenticationRequestTO
import com.taxiapp.driver_auth.service.DriverAuthenticationServiceImpl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile


@RestController
@RequestMapping("/api/driver-auth")
class DriverController(
    private val driverAuthenticationService: DriverAuthenticationServiceImpl
) {

    @GetMapping("/status")
    fun getDriverAuthenticationStatus(): ResponseEntity<Any> {
        val principal = SecurityContextHolder.getContext().authentication
        return driverAuthenticationService.getAuthenticationStatus(principal.name).let { result ->
            if (result.isSuccess()) {
                return ResponseEntity.ok(result)
            }
            ResponseEntity.status(result.httpStatus).body(result.messages)
        }
    }

    @PostMapping("/")
    fun submitDriverAuthentication(
        @RequestPart driverLicenseFrontPhoto: MultipartFile,
        @RequestPart driverLicenseBackPhoto: MultipartFile,
        @RequestPart request: DriverAuthenticationRequestTO
    ): ResponseEntity<Any> {
        val principal = SecurityContextHolder.getContext().authentication
        val resultTO = driverAuthenticationService.submitDriverAuthentication(
            principal.name,
            request,
            driverLicenseFrontPhoto,
            driverLicenseBackPhoto
        )
        return ResponseEntity.status(resultTO.httpStatus).body(if (!resultTO.isSuccess())resultTO.messages else null)
    }

    @DeleteMapping("/")
    fun cancelDriverAuthentication(): ResponseEntity<Any> {
        val principal = SecurityContextHolder.getContext().authentication
        val resultTO = driverAuthenticationService.cancelDriverAuthentication(principal.name)
        return ResponseEntity.status(resultTO.httpStatus).body(if (!resultTO.isSuccess())resultTO.messages else null)
    }


}