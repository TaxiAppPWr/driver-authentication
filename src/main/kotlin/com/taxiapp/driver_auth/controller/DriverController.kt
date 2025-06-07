package com.taxiapp.driver_auth.controller

import com.taxiapp.driver_auth.dto.request.DriverAuthenticationRequestTO
import com.taxiapp.driver_auth.service.DriverAuthenticationServiceImpl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile


@RestController
@RequestMapping("/api/driver-auth")
class DriverController(
    private val driverAuthenticationService: DriverAuthenticationServiceImpl
) {

    @GetMapping("/status")
    fun getDriverAuthenticationStatus(@RequestHeader username: String): ResponseEntity<Any> {
        return driverAuthenticationService.getAuthenticationStatus(username).let { result ->
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
        @RequestPart request: DriverAuthenticationRequestTO,
        @RequestHeader username: String
    ): ResponseEntity<Any> {
        val resultTO = driverAuthenticationService.submitDriverAuthentication(
            username,
            request,
            driverLicenseFrontPhoto,
            driverLicenseBackPhoto
        )
        return ResponseEntity.status(resultTO.httpStatus).body(if (!resultTO.isSuccess())resultTO.messages else null)
    }

    @DeleteMapping("/")
    fun cancelDriverAuthentication(@RequestHeader username: String): ResponseEntity<Any> {
        val resultTO = driverAuthenticationService.cancelDriverAuthentication(username)
        return ResponseEntity.status(resultTO.httpStatus).body(if (!resultTO.isSuccess())resultTO.messages else null)
    }

    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf(
            "status" to "UP",
            "service" to "driver-authentication-service"
        ))
    }

}