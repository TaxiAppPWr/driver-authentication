package com.taxiapp.driver_auth.controller

import com.taxiapp.driver_auth.dto.request.DriverAuthenticationRequestTO
import com.taxiapp.driver_auth.service.DriverAuthenticationServiceImpl
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.security.Principal

@Controller("/api/driver-auth")
class DriverController(
    private val driverAuthenticationService: DriverAuthenticationServiceImpl
) {

    @GetMapping("/status")
    fun getDriverAuthenticationStatus(principal: Principal): ResponseEntity<Any> {
        return driverAuthenticationService.getAuthenticationStatus(principal.name).let { result ->
            if (result.isSuccess()) {
                return ResponseEntity.ok(result)
            }
            ResponseEntity.status(result.httpStatus).body(result.messages)
        }
    }

    @PostMapping("/", consumes = ["multipart/form-data"])
    fun submitDriverAuthentication(principal: Principal,
                           @RequestBody requestTO: DriverAuthenticationRequestTO): ResponseEntity<Any> {
        val resultTO = driverAuthenticationService.submitDriverAuthentication(
            principal.name,
            requestTO
        )
        return ResponseEntity.status(resultTO.httpStatus).body(if (!resultTO.isSuccess())resultTO.messages else null)
    }

    @DeleteMapping("/")
    fun cancelDriverAuthentication(principal: Principal): ResponseEntity<Any> {
        val resultTO = driverAuthenticationService.cancelDriverAuthentication(principal.name)
        return ResponseEntity.status(resultTO.httpStatus).body(if (!resultTO.isSuccess())resultTO.messages else null)
    }


}