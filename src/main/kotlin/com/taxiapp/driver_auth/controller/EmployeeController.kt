package com.taxiapp.driver_auth.controller

import com.taxiapp.driver_auth.service.DriverAuthenticationService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/employee")
class EmployeeController(
    private val driverAuthenticationService: DriverAuthenticationService
) {
    @GetMapping("/pending-verifications")
    fun getPendingVerifications(): ResponseEntity<Any> {
        return driverAuthenticationService.getPendingVerifications().let { result ->
            if (result.isSuccess()) {
                return ResponseEntity.ok(result)
            }
            ResponseEntity.status(result.httpStatus).body(result.messages)
        }
    }

    @GetMapping("/pending-verifications/{id}")
    fun getPendingVerificationById(@PathVariable id: Long): ResponseEntity<Any> {
        return driverAuthenticationService.getPendingVerificationById(id).let { result ->
            if (result.isSuccess()) {
                return ResponseEntity.ok(result)
            }
            ResponseEntity.status(result.httpStatus).body(result.messages)
        }
    }

    @PostMapping("/pending-verifications/{id}/approve")
    fun approveVerification(@PathVariable id: Long): ResponseEntity<Any> {
        return driverAuthenticationService.approveVerification(id).let { result ->
            if (result.isSuccess()) {
                return ResponseEntity.ok().build()
            }
            ResponseEntity.status(result.httpStatus).body(result.messages)
        }
    }

    @PostMapping("/pending-verifications/{id}/reject")
    fun rejectVerification(@PathVariable id: Long): ResponseEntity<Any> {
        return driverAuthenticationService.rejectVerification(id).let { result ->
            if (result.isSuccess()) {
                return ResponseEntity.ok().build()
            }
            ResponseEntity.status(result.httpStatus).body(result.messages)
        }
    }
}