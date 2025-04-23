package com.taxiapp.driver_auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class DriverAuthenticationApp

fun main(args: Array<String>) {
    runApplication<DriverAuthenticationApp>(*args)
}